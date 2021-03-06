package com.tpnet.downmanager.download.listener;

/**
 * 回调到view的下载过程接口。使用更灵活的抽象类方法，选择性回调
 * Created by litp on 2017/4/10.
 */

public abstract class IOnDownloadListener<T> {

    /**
     * 成功后回调方法
     * @param t 实体
     */
    public abstract void onNext(T t);

    /**
     * 开始下载
     */
    public abstract void onStart();

    /**
     * 完成下载
     */
    public abstract void onComplete();



    /**
     * 下载进度，这个是拼命回调
     * @param downLength
     * @param totalLength
     */
    public abstract void updateLength(long downLength, long totalLength,int percent);


    /**
     * 下载进度，100，每个进度回调一次
     * @param percent
     */
    public abstract void updatePercent(int percent);
    
    
    
    /**
     * 失败或者错误
     * @param e 异常信息
     */
    public  void onError(Throwable e){

    }

    /**
     * 暂停下载
     */
    public void onPuase(){

    }

    /**
     * 停止下载
     */
    public void onStop(){

    }

    /**
     * 文件长度返回
     */
    public void onLenght(){}
    
    
}
