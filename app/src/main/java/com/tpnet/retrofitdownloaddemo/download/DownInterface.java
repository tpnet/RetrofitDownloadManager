package com.tpnet.retrofitdownloaddemo.download;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Retrofit下载接口
 * Created by litp on 2017/4/17.
 */

public interface DownInterface {

    //在头部添加RANGE，实现断点续传
    @Streaming //大文件需要加入这个判断，防止下载过程中写入到内存中,防止oom
    @GET
    Observable<ResponseBody> download(@Header("RANGE") String start, @Url String url);
}

