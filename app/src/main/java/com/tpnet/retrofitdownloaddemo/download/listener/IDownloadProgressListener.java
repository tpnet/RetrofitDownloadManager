package com.tpnet.retrofitdownloaddemo.download.listener;

/**
 * 下载进度接口
 * Created by litp on 2017/4/10.
 */

public interface IDownloadProgressListener {

    /**
     * 下载进度
     * @param down 下载的数量
     * @param total 文件的总长度
     * @param finish 是否下载完成
     */
    void update(long down,long total,boolean finish);
    
    void updateTotalLength(long totalLength);
    
    
    void updateDowning();
}
