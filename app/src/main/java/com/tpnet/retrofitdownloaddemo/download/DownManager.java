package com.tpnet.retrofitdownloaddemo.download;

import android.util.Log;

import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.download.exception.RetryWhenNetworkException;
import com.tpnet.retrofitdownloaddemo.download.listener.DownService;
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

    // 记录下载数据
    private Map<String, DownInfo> downInfos;


    //回调观察者队列,downUrl标识，暂停和错误都会清除对应的观察者
    private Map<String, DownSubscriber<DownInfo>> downSubs;

    //单例
    private volatile static DownManager INSTANCE;


    public DownManager() {
        downInfos = new HashMap<>();
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


    //添加view回调监听器
    public void addListener(String downUrl, IOnDownloadListener<DownInfo> listener) {
        if (downSubs.get(downUrl) != null) {
            Log.e("@@", "添加监听器" + downUrl);
            downSubs.get(downUrl).setListener(listener);
        }
    }


    public void startDown(final DownInfo info) {
        //正在下载不处理
        if (info == null) {
            return;
        }

        //添加回调处理类
        DownSubscriber<DownInfo> subscriber;

        if (downSubs.get(info.downUrl()) != null) {

            //添加监听器
            if (info.getListener() != null) {
                downInfos.put(info.downUrl(),
                        DownInfo.create(downInfos.get(info.downUrl())).build().setListener(info.getListener())
                );
                downInfos.get(info.downUrl()).setListener(info.getListener());
                downSubs.get(info.downUrl()).setListener(info.getListener());
            }

            if (info.downState() == DownInfo.DOWN_ING) {
                return;
            }

            subscriber = downSubs.get(info.downUrl());

        } else {
            subscriber = new DownSubscriber<DownInfo>(info);
            downSubs.put(info.downUrl(), subscriber);

        }

        DownService service;

        if (downInfos.get(info.downUrl()) != null) {
            //获取service
            service = downInfos.get(info.downUrl()).getService();
        } else {

            service = createService(new DownloadInterceptor(subscriber));

            info.setService(service);

            downInfos.put(info.downUrl(), info);

            //插入数据库
            DatabaseUtil.getInstance().insertDownInfo(info);

        }


        // 断点下载
        service.download("bytes=" + downInfos.get(info.downUrl()).downLength() + "-", downInfos.get(info.downUrl()).downUrl())
                /*指定线程*/
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .retryWhen(new RetryWhenNetworkException())   //失败重试
                .map(new Func1<ResponseBody, DownInfo>() {    //写入文件
                    @Override
                    public DownInfo call(ResponseBody responseBody) {

                        Log.e("@@", "数据回调map call保存到文件: contentLength=" + responseBody.contentLength());

                        try {
                            FileUtil.writeFile(responseBody, new File(downInfos.get(info.downUrl()).savePath()), downInfos.get(info.downUrl()));
                        } catch (IOException e) {
                            //throw e;
                            Log.e("@@", "写入文件错误" + e.getMessage());
                        }

                        return downInfos.get(info.downUrl());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) //回调在主线程
                .subscribe(subscriber);    //数据回调

        Log.e("@@", "开始下载");


    }


    private DownService createService(Interceptor interceptor) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Constant.TIME_OUT, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();


        //创建Retrofit
        return new Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("https://www.baidu.com/")
                .build()
                .create(DownService.class);
    }


    /**
     * 停止下载,进度设置为0，状态未开始
     *
     * @param info
     */
    public void stopDown(DownInfo info) {
        if(info.getListener() != null){
            info.getListener().onStop();
        }
        handleDown(info, DownInfo.DOWN_STOP);
    }


    /**
     * 下载错误
     *
     * @param info
     */
    public void errorDown(DownInfo info, Throwable e) {
        if(info.getListener() != null){
            info.getListener().onError(e);
        }
        handleDown(info, DownInfo.DOWN_ERROR);
    }

    /**
     * 暂停下载
     *
     * @param info
     */
    public void pauseDown(DownInfo info) {
        if(info.getListener() != null){
            info.getListener().onPuase();
        }
        handleDown(info, DownInfo.DOWN_PAUSE);
    }

    //处理下载状态
    private void handleDown(DownInfo info, @DownState int state) {
        if (info == null) return;

        info = DownInfo.create(info).downState(state).build();


        if (downSubs.get(info.downUrl()) != null) {

            downSubs.get(info.downUrl()).unsubscribe();
            downSubs.remove(info.downUrl());
        }

        DatabaseUtil.getInstance()
                .updateState(state, info.downUrl());
    }


    /**
     * 停止全部下载
     */
    public void stopAllDown() {

        for (String key : downInfos.keySet()) {
            stopDown(downInfos.get(key));
        }

        downSubs.clear();
        downInfos.clear();
    }


    /**
     * 暂停全部下载
     */
    public void pauseAllDown() {

        for (String key : downInfos.keySet()) {
            pauseDown(downInfos.get(key));
        }

        downSubs.clear();
        downInfos.clear();
    }


    /**
     * 移除下载数据
     *
     * @param info
     */
    public void remove(DownInfo info) {
        downSubs.remove(info.downUrl());
        downInfos.remove(info.downUrl());
    }


}
