package com.tpnet.downmanager.download;

import android.util.Log;

import com.tpnet.downmanager.download.listener.DownInterface;
import com.tpnet.downmanager.download.listener.IOnDownloadListener;
import com.tpnet.downmanager.download.rxbus.Events;
import com.tpnet.downmanager.download.rxbus.ProgressEvent;
import com.tpnet.downmanager.download.rxbus.RxBus;
import com.tpnet.downmanager.utils.ToastUtil;

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

        //更新bean的下载完成时间,可更新可以不更新

        DownManager.getInstance().onStartDown(downInfo.downUrl());
        

    }


    /**
     * 完成回调，这个是Rxjava的发射回调，不做处理，
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

        DownManager.getInstance().errorDown(downInfo, e);

    }


    /**
     * 下载完成
     * @param t
     */
    @Override
    public void onNext(T t) {
        Log.e("@@", "onNext下载完毕");
        //ToastUtil.show("下载完毕");

        //更新bean的下载完成时间,状态,可更新可以不更新，成功回调onNext
        DownManager.getInstance().onFinishDown(downInfo.downUrl());
        
    }

    //在DownManager调用
    public void onNext() {
        if (listener != null && listener.get() != null) {
            listener.get().onNext(downInfo);
        }
        setDownloadState(DownInfo.DOWN_FINISH);
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

        //Log.e("@@", "下载长度" + downLength);
        
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
                            DownManager.getInstance()
                                    .onSetDownLength(downInfo.downLength(), downInfo.downUrl());
                            
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


    /**
     * 设置当前的下载状态
     *
     * @param state
     */
    public void setDownloadState(@DownState int state) {

        Log.e("@@", "sub更新状态" + state);

        this.downInfo = DownInfo.create(downInfo)
                .downState(state)
                .build();


        //开始，暂停，错误时候，更新下载长度到数据库
        DownManager.getInstance()
                .onSetDownLength(downInfo.downLength(), downInfo.downUrl());
    }


}
