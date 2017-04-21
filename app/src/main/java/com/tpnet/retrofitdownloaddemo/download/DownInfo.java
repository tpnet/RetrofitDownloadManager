package com.tpnet.retrofitdownloaddemo.download;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.squareup.sqldelight.RowMapper;
import com.tpnet.retrofitdownloaddemo.DownInfoModel;
import com.tpnet.retrofitdownloaddemo.download.listener.DownService;
import com.tpnet.retrofitdownloaddemo.download.listener.IOnDownloadListener;

/**
 * 下载的信息
 * Created by litp on 2017/4/10.
 */

@AutoValue
public abstract class DownInfo implements Parcelable,DownInfoModel{
    
    //下面6中下载状态,数字需要改变的话，Sql语句里面也需要改变
    public final static int DOWN_START = 0x0;    //开始
    public final static int DOWN_PAUSE = 0x1;    //暂停
    public final static int DOWN_STOP = 0x2;     //停止
    public final static int DOWN_ERROR = 0x3;     //错误
    public final static int DOWN_ING = 0x4;         //下载中
    public final static int DOWN_FINISH = 0x5;      //完成
    
    
    
    private DownService service;
    
    private IOnDownloadListener<DownInfo> listener;


    public DownService getService() {
        return service;
    }

    public DownInfo setService(DownService service) {
        this.service = service;
        return this;
    }

    public IOnDownloadListener<DownInfo> getListener() {
        return listener;
    }
    
    public DownInfo setListener(IOnDownloadListener<DownInfo> listener) {
        this.listener = listener;
        return this;
    }
    
    
    public DownInfo addListener(IOnDownloadListener<DownInfo> listener){
        this.listener = listener;
        //设置监听器到DownSubscriber
        DownManager.getInstance().addListener(this.downUrl(),listener);
        return this;
    }



    
    public static final Factory<DownInfo> FACTORY = new Factory<>(new DownInfoModel.Creator<DownInfo>() {
        @Override
        public DownInfo create(@NonNull long _id,@NonNull String downUrl, @NonNull String savePath, long totalLength, long downLength, int downState, long startTime, long finishTime) {
            return new AutoValue_DownInfo(_id,downUrl,savePath,totalLength,downLength,downState,startTime,finishTime);
        }
    });
    
    
    public static final RowMapper<DownInfo> LIST_ROW_MAPPER = FACTORY.selectAllMapper();
    
    
    public static final RowMapper<String> LIST_EXIST_MAPPER = FACTORY.selectDowninfoSavePathMapper();
    
    
    
    
    public static DownInfo create(String savePath, long totalLength, long downLength, int downState, String downUrl, long startTime, long finishTime, long pauseTime, long allTime) {
        return builder()
                ._id(0)
                .savePath(savePath)
                .totalLength(totalLength)
                .downLength(downLength)
                .downState(downState)
                .downUrl(downUrl)
                .startTime(startTime)
                .finishTime(finishTime)
                .build();
    }


    public static Builder create(DownInfo downInfo) {
        return builder()
                ._id(downInfo._id())
                .savePath(downInfo.savePath())
                .totalLength(downInfo.totalLength())
                .downLength(downInfo.downLength())
                .downState(downInfo.downState())
                .downUrl(downInfo.downUrl())
                .startTime(downInfo.startTime())
                .finishTime(downInfo.finishTime());
    }


    

    public static Builder builder() {
        return new AutoValue_DownInfo.Builder();
    }


    @AutoValue.Builder
    public abstract static class Builder {
      
        public abstract Builder _id(long id);
        
        public abstract Builder savePath(String savePath);

        public abstract Builder totalLength(long totalLength);

        public abstract Builder downLength(long downLength);

        public abstract Builder downState(@DownState int downState);

        public abstract Builder downUrl(String downUrl);

        public abstract Builder startTime(long startTime);

        public abstract Builder finishTime(long finishTime);
        
        
        public abstract DownInfo build();

        //创建任务初始化其他参数
        public DownInfo create(){
            
            _id(0);
            
            startTime(0);
            
            finishTime(0);
            
            totalLength(0);
            
            downLength(0);
            
            downState(DOWN_START);
            
            return build();
        }
        
    }
}
