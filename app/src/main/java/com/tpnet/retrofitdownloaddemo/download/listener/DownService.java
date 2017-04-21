package com.tpnet.retrofitdownloaddemo.download.listener;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Retrofit的数据接口，这里是下载
 * Created by litp on 2017/4/10.
 */

public interface DownService {

    /**
     * 下载文件的Retrofit接口
     * @param range 添加RANGE这个参数实现断点续传
     * @param url 下载的链接
     * @return 返回Rxjava处理
     */
    @Streaming  //大文件需要加这个注解，其实是不让写入到内存中。不加的话，大文件报IOException
    @GET         
    Observable<ResponseBody> download(@Header("Range") String range, @Url String url);
    
}
