package com.tpnet.retrofitdownloaddemo.download;

import android.util.Log;

import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.download.listener.DownInterface;
import com.tpnet.retrofitdownloaddemo.download.listener.IOnDownloadListener;
import com.tpnet.retrofitdownloaddemo.utils.Constant;
import com.tpnet.retrofitdownloaddemo.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 统一下载管理器
 * Created by litp on 2017/4/10.
 */

public class DownManager {


    //回调观察者队列,downUrl标识，暂停和错误都会取消订阅对应的观察者
    private Map<String, DownSubscriber<DownInfo>> downSubs;


    //单例
    private volatile static DownManager INSTANCE;


    public DownManager() {
        downSubs = new HashMap<>();

    }


    /**
     * 获取单例
     */
    public static DownManager getInstance() {
        if (INSTANCE == null) {
            synchronized (DownManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DownManager();

                }
            }
        }
        return INSTANCE;
    }


    public void startDown(final DownInfo info) {
        //正在下载不处理
        if (info == null) {
            return;
        }

        //添加回调处理类
        DownSubscriber<DownInfo> subscriber;

        if (downSubs.get(info.downUrl()) != null) {  //切换界面下载 情况

            if (downSubs.get(info.downUrl()).getDownInfo().downState() == DownInfo.DOWN_ING) {
                //切换界面下载中，返回，继续下载
                return;
            }
            
            if(downSubs.get(info.downUrl()).isUnsubscribed()){  //重试下载
                Log.e("@@","重试下载");
                subscriber = new DownSubscriber<DownInfo>(
                        downSubs.get(info.downUrl()).getListener()
                ,downSubs.get(info.downUrl()).getDownInfo()
                ,downSubs.get(info.downUrl()).getService()
                ,downSubs.get(info.downUrl()).getPrePercent());
                
                downSubs.remove(info.downUrl());
                //覆盖订阅者
                downSubs.put(info.downUrl(), subscriber);
            }else{
                subscriber = downSubs.get(info.downUrl());
            }
            
        } else {  //第一次下载

            subscriber = new DownSubscriber<DownInfo>(info);
            //更新订阅者
            downSubs.put(info.downUrl(), subscriber);
        }

        
        DownInterface service;

        if (downSubs.get(info.downUrl()).getService() != null) {
            //获取service
            service = downSubs.get(info.downUrl()).getService();
        } else {

            service = createService(new DownloadInterceptor(info.downUrl()));

            downSubs.get(info.downUrl()).setService(service);

            //插入数据库
            DatabaseUtil.getInstance().insertDownInfo(downSubs.get(info.downUrl()).getDownInfo());

        }


        Log.e("@@", "断点续传长度为：" + downSubs.get(info.downUrl()).getDownInfo().downLength());
        // 断点下载,每次返回的总长度是减去断点续传的长度
        service.download("bytes=" + downSubs.get(info.downUrl()).getDownInfo().downLength() + "-", downSubs.get(info.downUrl()).getDownInfo().downUrl())
                //在线程中下载
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Func1<ResponseBody, DownInfo>() {    //写入文件
                    @Override
                    public DownInfo call(ResponseBody responseBody) {

                        Log.e("@@", "数据回调map call保存到文件: contentLength=" + responseBody.contentLength());

                        //更新总长度
                        downSubs.get(info.downUrl()).updateTotalLength(responseBody.contentLength());
                        //更新下载中状态
                        downSubs.get(info.downUrl()).updateDowning();

                        try {
                            FileUtil.writeFile(responseBody, new File(downSubs.get(info.downUrl()).getDownInfo().savePath()), downSubs.get(info.downUrl()).getDownInfo());
                        } catch (IOException e) {
                            //throw e;
                            Log.e("@@", "写入文件错误" + e.getMessage());
                        }

                        return downSubs.get(info.downUrl()).getDownInfo();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) //回调在主线程
                .subscribe(subscriber);    //数据回调

        Log.e("@@", "开始下载");


    }


    private DownInterface createService(Interceptor interceptor) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Constant.TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();


        //创建Retrofit
        return new Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("https://www.baidu.com/")  //下载文件，基地址可以不用正确，下载文件的url是全路径即可
                .build()
                .create(DownInterface.class);
    }


    //添加view回调监听器
    public void addListener(String downUrl, IOnDownloadListener<DownInfo> listener) {
        if (downSubs.get(downUrl) != null) {
            Log.e("@@", "添加监听器" + downUrl);
            downSubs.get(downUrl).setListener(listener);

        }
    }


    /**
     * 停止下载,进度设置为0，状态未开始
     *
     * @param info
     */
    public void stopDown(final DownInfo info) {


        if (handleDown(info, DownInfo.DOWN_STOP) > 0) {
            if (downSubs.get(info.downUrl()).getListener() != null) {
                downSubs.get(info.downUrl()).getListener().onStop();
            }
        }

    }


    /**
     * 下载错误
     *
     * @param info
     */
    public void errorDown(final DownInfo info, final Throwable e) {

        if (handleDown(info, DownInfo.DOWN_ERROR) > 0) {
            if (downSubs.get(info.downUrl()).getListener() != null) {
                downSubs.get(info.downUrl()).getListener().onError(e);
            }
        }

    }

    /**
     * 暂停下载
     *
     * @param info
     */
    public void pauseDown(DownInfo info) {

        if (handleDown(info, DownInfo.DOWN_PAUSE) > 0) {
            if (downSubs.get(info.downUrl()).getListener() != null) {
                downSubs.get(info.downUrl()).getListener().onPuase();
            }
        }

    }

    //处理下载状态
    private Integer handleDown(DownInfo info, @DownState int state) {
        if (info == null) return null;

        if (downSubs.get(info.downUrl()) != null) {
            
            //解除订阅就不会下载了
            downSubs.get(info.downUrl()).unsubscribe();

            downSubs.get(info.downUrl()).setDownloadState(state);

            //防止下载速度太快导致继续下载回调
            //downSubs.get(info.downUrl()).setListener(null);
            //downSubs.remove(info.downUrl());

        }

        return DatabaseUtil.getInstance()
                .updateState(state, info.downUrl());
    }


    /**
     * 停止全部下载
     */
    public void stopAllDown() {


        for (String key : downSubs.keySet()) {
            stopDown(downSubs.get(key).getDownInfo());
        }

        downSubs.clear();
    }


    /**
     * 暂停全部下载
     */
    public void pauseAllDown() {

        for (String key : downSubs.keySet()) {
            pauseDown(downSubs.get(key).getDownInfo());
        }

        downSubs.clear();
    }


    /**
     * 移除下载数据
     *
     * @param info
     */
    public void remove(DownInfo info) {
        downSubs.remove(info.downUrl());
    }

 
}
