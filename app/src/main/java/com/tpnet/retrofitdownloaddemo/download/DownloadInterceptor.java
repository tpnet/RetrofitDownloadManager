package com.tpnet.retrofitdownloaddemo.download;

import com.tpnet.retrofitdownloaddemo.download.listener.IDownloadProgressListener;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 下载拦截器，在RespongseBody添加下载监听接口
 * Created by litp on 2017/4/10.
 */

public class DownloadInterceptor implements Interceptor {
    
    
    private IDownloadProgressListener listener;

    //回调
    public DownloadInterceptor(IDownloadProgressListener listener) {
        this.listener = listener;
    }

   

    @Override
    public Response intercept(Chain chain) throws IOException {
        
        Response response = chain.proceed(chain.request());

        return response.newBuilder()
                .body(new DownloadResponseBody(response.body(),listener))
                .build();
    }
    
}
