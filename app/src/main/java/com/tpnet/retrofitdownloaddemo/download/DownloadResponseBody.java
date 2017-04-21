package com.tpnet.retrofitdownloaddemo.download;

import com.tpnet.retrofitdownloaddemo.download.listener.IDownloadProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 自定义的进度body
 * Created by litp on 2017/4/10.
 */

public class DownloadResponseBody extends ResponseBody{

    //返回主体
    private ResponseBody responseBody;

    //下载进度回调
    private IDownloadProgressListener progressListener;   //下载进度监听器

    private BufferedSource bufferedSource;


    /**
     * 构造
     * @param responseBody 原始的responseBody
     //* @param progressListener 下线进度接口
     */
    public DownloadResponseBody(ResponseBody responseBody, IDownloadProgressListener progressListener) {
        this.responseBody = responseBody;
     
        this.progressListener = progressListener;
        
        //更新数据长度，返回的总长度是减去断点续传的长度
        progressListener.updateTotalLength(responseBody.contentLength());
        progressListener.updateDowning();
        
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 处理缓存源
     * @param source
     * @return
     */
    private Source source(Source source) {
        return new ForwardingSource(source) {
            
            long downBytes = 0L;   //已经下载的字节

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                
                
                
                //初始化已经下载的字节数
                long bytesRead = super.read(sink, byteCount);
                
                //添加
                downBytes += bytesRead != -1 ? bytesRead : 0;
                
                //回调接口到Subscriber
                if (null != progressListener) {
                    progressListener.update(downBytes, responseBody.contentLength(), bytesRead == -1 && downBytes == responseBody.contentLength());
                }
                
                return bytesRead;
            }
        };

    }
    
}
