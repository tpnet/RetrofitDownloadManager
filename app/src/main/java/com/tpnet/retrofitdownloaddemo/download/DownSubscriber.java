package com.tpnet.retrofitdownloaddemo.download;

import android.util.Log;

import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.download.listener.IDownloadProgressListener;
import com.tpnet.retrofitdownloaddemo.download.listener.IOnDownloadListener;

import java.lang.ref.SoftReference;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * 下载的Rxjava观察者
 * Created by litp on 2017/4/10.
 */

public class DownSubscriber<T> extends Subscriber<T> implements IDownloadProgressListener{


    //软件引用结果回调，内存不足回收
    private SoftReference<IOnDownloadListener> listener;

    private DownInfo downInfo;

 

    public DownSubscriber(DownInfo downInfo) {
        setDownInfo(downInfo);
    }


    //到了下载的列表界面就设置监听器
    public void setListener(IOnDownloadListener listener) {
        downInfo.listener = listener;
        this.listener = new SoftReference<IOnDownloadListener>(downInfo.getListener());
    }

    
    public IOnDownloadListener getListener(){
        return listener != null ? listener.get() : null;
    }

    public void setDownInfo(DownInfo data) {

        this.downInfo = data;
        
        if (downInfo.getListener() != null) {
            this.listener = new SoftReference<IOnDownloadListener>(downInfo.getListener());
        }

        
    }

    public DownInfo getDownInfo() {
        return downInfo;
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

        //AutoValue标注的bean不能setter，需要重新new一个
        downInfo = DownInfo.create(downInfo).downState(DownInfo.DOWN_START).build();
        //更新数据库
        DatabaseUtil.getInstance().updateState(DownInfo.DOWN_START, downInfo.downUrl());

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

        DownManager.getInstance().remove(downInfo);

        //AutoValue标注的bean不能setter，需要重新new一个
        downInfo = DownInfo.create(downInfo).downState(DownInfo.DOWN_FINISH).build();

        //更新状态
        DatabaseUtil.getInstance().updateState(DownInfo.DOWN_FINISH, downInfo.downUrl());

    }

    /**
     * 下载错误
     *
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        Log.e("@@", "onErro下载失败: " + e.toString());
        
        DownManager.getInstance().errorDown(downInfo,e);

    }

    
    

    /**
     * @param t
     */
    @Override
    public void onNext(T t) {
        Log.e("@@", "onNext下载完毕");
        if (listener != null && listener.get() != null) {
            listener.get().onNext(t);
        }
    }


    //下载进度回调
    @Override
    public void update(final long down, final long total, boolean finish) {
        
        //Log.e("@@","下载进度: downBytes="+down+" responseBody.contentLength()="+total);
        
        long downLength = down;
        
        
        //设置当前下载状态
        DownInfo.Builder builder = DownInfo.create(downInfo);
        if (downInfo.totalLength() > total) {
            downLength = downInfo.totalLength() - total + down;
        } else {
            builder.totalLength(total);
        }
        downInfo = builder
                .downLength(downLength)
                .downState(DownInfo.DOWN_ING)
                .build();


        //更新数据库
        DatabaseUtil.getInstance()
                .updateDownLength(down, downInfo.downUrl());
        
        //回调到view
        if (listener != null && listener.get() != null) {

            //接受进度消息，造成UI阻塞，如果不需要显示进度可去掉实现逻辑，减少压力
            Observable.just(down)
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(new Func1<Long, Boolean>() {
                        @Override
                        public Boolean call(Long aLong) {
                            //如果暂停或者停止状态延迟，不需要继续发送回调，影响显示
                            return downInfo.downState() != DownInfo.DOWN_PAUSE && downInfo.downState() != DownInfo.DOWN_STOP;
                        }
                    })
                    .map(new Func1<Long, Integer>() {
                        @Override
                        public Integer call(Long aLong) {
                            //下载百分比
                            return (int)(down / total * 100);
                        }
                    })
                    .distinct()  //过滤重复的进度，减少压力
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer percent) {
                         
                            listener.get().updateProgress(down, total,percent);

                        }
                    });
        }

    }

    @Override
    public void updateTotalLength(long totalLength) {
        Log.e("@@","文件长度:"+totalLength);
        DatabaseUtil.getInstance().updateTotalLength(totalLength,downInfo.downUrl());

    }

    //更新下载中状态
    @Override
    public void updateDowning() {
        DatabaseUtil.getInstance().updateState(DownInfo.DOWN_ING, downInfo.downUrl());
    }
}
