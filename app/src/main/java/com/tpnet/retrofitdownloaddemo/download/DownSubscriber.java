package com.tpnet.retrofitdownloaddemo.download;

import android.util.Log;

import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.download.listener.DownInterface;
import com.tpnet.retrofitdownloaddemo.download.listener.IOnDownloadListener;
import com.tpnet.retrofitdownloaddemo.download.rxbus.Events;
import com.tpnet.retrofitdownloaddemo.download.rxbus.ProgressEvent;
import com.tpnet.retrofitdownloaddemo.download.rxbus.RxBus;
import com.tpnet.retrofitdownloaddemo.utils.ToastUtil;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * 下载的Rxjava观察者
 * Created by litp on 2017/4/10.
 */

public class DownSubscriber<T> extends Subscriber<T> {


    //弱引用结果回调，挂了就回收。  回调view监听器
    private WeakReference<IOnDownloadListener> listener;

    private DownInfo downInfo;   //下载bean


    private DownInterface service;     // Retrofit的服务端


    private int prePercent = 0;  //上一次的进度，防止频繁更新View


    private long preDownLength;  // 上一次下载的进度


    public DownSubscriber(DownInfo downInfo) {
        this(null, downInfo, null, 0);
    }


    public DownSubscriber(IOnDownloadListener listener, DownInfo downInfo, DownInterface service, int prePercent) {
        setListener(listener);
        setDownInfo(downInfo);
        setService(service);
        setPrePercent(prePercent);

        //接收下载过程中的进度回调
        RxBus.with().setEvent(downInfo.downUrl())
                .onNext(new Action1<Events<?>>() {
                    @Override
                    public void call(Events<?> events) {
                        ProgressEvent event = events.getContent();
                        update(event.getDownLength(), event.getTotalLength(), event.isFinish());
                    }
                })
                .create();

    }


    public int getPrePercent() {
        return prePercent;
    }

    public void setPrePercent(int prePercent) {
        this.prePercent = prePercent;
    }

    //到了下载的列表界面就设置监听器
    public void setListener(IOnDownloadListener listener) {
        this.listener = new WeakReference<IOnDownloadListener>(listener);
    }


    public IOnDownloadListener getListener() {
        return listener != null ? listener.get() : null;
    }

    public void setDownInfo(DownInfo data) {

        this.downInfo = data;

    }

    public DownInfo getDownInfo() {
        return downInfo;
    }


    public DownInterface getService() {
        return service;
    }

    public void setService(DownInterface service) {
        this.service = service;
    }

    /**
     * 开始下载
     */
    @Override
    public void onStart() {
        super.onStart();
        if (listener != null && listener.get() != null) {
            //回调开始
            listener.get().onStart();
        }

        Log.e("@@", "Subscriber onStart开始下载");
        ToastUtil.show("开始下载");

        setDownloadState(DownInfo.DOWN_START);

        Log.e("@@", "开始更新状态为开始");

        //更新数据库
        DatabaseUtil.getInstance()
                .updateState(DownInfo.DOWN_START, downInfo.downUrl());

    }


    /**
     * 下载完成
     */
    @Override
    public void onCompleted() {
        if (listener != null && listener.get() != null) {
            //回调开始
            listener.get().onComplete();
        }

        Log.e("@@", "onCompleted完成下载");


    }

    /**
     * 下载错误
     *
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        Log.e("@@", "onErro下载失败: " + e.toString());
        ToastUtil.show("下载错误");

        setDownloadState(DownInfo.DOWN_ERROR);

        DownManager.getInstance().errorDown(downInfo, e);

    }


    /**
     * @param t
     */
    @Override
    public void onNext(T t) {
        Log.e("@@", "onNext下载完毕");
        ToastUtil.show("下载完毕");

        if (listener != null && listener.get() != null) {
            listener.get().onNext(t);
        }

        //AutoValue标注的bean不能setter，需要重新new一个
        setDownloadState(DownInfo.DOWN_FINISH);

        //更新状态
        DatabaseUtil.getInstance()
                .updateState(DownInfo.DOWN_FINISH, downInfo.downUrl());


        DownManager.getInstance().remove(downInfo);
    }


    //下载进度回调
    public void update(long down, final long total, final boolean finish) {

        //Log.e("@@","下载进度: downBytes="+down+" responseBody.contentLength()="+total);

        long downLength = down;


        //设置当前下载状态
        DownInfo.Builder builder = DownInfo.create(downInfo);
        if (downInfo.totalLength() > total) {
            //断点续传下载的情况
            downLength = downInfo.totalLength() - total + down;
        } else if (downInfo.totalLength() < total) {
            builder.totalLength(total);
        }
        
        Log.e("@@", "下载长度" + downLength);
        
        //如果已经解除订阅了，代表暂停停止出错了，不更新状态了
        if (!isUnsubscribed()) {
            //Log.e("@@","设置下载中状态");
            builder.downState(DownInfo.DOWN_ING);
        }

        downInfo = builder
                .downLength(downLength)
                .build();
        
        Observable.just(downInfo.downLength())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long aLong) {
                        
                        
                        return (int) (100 * downInfo.downLength() / downInfo.totalLength());

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer percent) {
                        
                        
                        if (percent > prePercent) {
                            
                            prePercent = percent;

                            //更新下载长度到数据库
                            DatabaseUtil.getInstance()
                                    .updateDownLength(downInfo.downLength(), downInfo.downUrl());
                            
                            if(listener != null && listener.get() != null && !isUnsubscribed()){
                                //回调进度
                                listener.get().updatePercent(percent);

                            }
                            
                            
                        }else{
                            if(listener != null && listener.get() != null && !isUnsubscribed()){
                                //回调长度
                                listener.get().updateLength(downInfo.downLength(), downInfo.totalLength(), percent);

                            }
                        }
                        
                        
                        
                        
                        
                            
                    }
                });

        

    }


    public void updateTotalLength(long totalLength) {
        Log.e("@@", "文件长度:" + totalLength);
        DatabaseUtil.getInstance().updateTotalLength(totalLength, downInfo.downUrl());

    }

    //更新下载中状态
    public void updateDowning() {
        DatabaseUtil.getInstance()
                .updateState(DownInfo.DOWN_ING, downInfo.downUrl());
    }


    public void setDownloadState(@DownState int state) {
        Log.e("@@", "sub更新状态" + state);
        this.downInfo = DownInfo.create(downInfo)
                .downState(state)
                .build();


        //更新下载长度到数据库
        DatabaseUtil.getInstance()
                .updateDownLength(downInfo.downLength(), downInfo.downUrl());
    }

}
