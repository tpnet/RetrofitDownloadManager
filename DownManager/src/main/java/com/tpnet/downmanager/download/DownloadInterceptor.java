package com.tpnet.downmanager.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 下载拦截器，在RespongseBody添加下载监听接口
 * Created by litp on 2017/4/10.
 */

public class DownloadInterceptor implements Interceptor {
    
    
    //private IDownloadProgressListener listener;

    private String downUrl;
    //回调
    public DownloadInterceptor(String downUrl/*IDownloadProgressListener listener*/) {
        //this.listener = listener;
        this.downUrl = downUrl;
    }

   

    @Override
    public Response intercept(Chain chain) throws IOException {
        
        Response response = chain.proceed(chain.request());

        return response.newBuilder()
                .body(new DownloadResponseBody(response.body(),downUrl/*,listener*/))
                .build();
    }
    
}
