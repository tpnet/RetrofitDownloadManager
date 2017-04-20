package com.tpnet.retrofitdownloaddemo.download;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.tpnet.retrofitdownloaddemo.download.DownInfo.DOWN_ERROR;
import static com.tpnet.retrofitdownloaddemo.download.DownInfo.DOWN_FINISH;
import static com.tpnet.retrofitdownloaddemo.download.DownInfo.DOWN_ING;
import static com.tpnet.retrofitdownloaddemo.download.DownInfo.DOWN_PAUSE;
import static com.tpnet.retrofitdownloaddemo.download.DownInfo.DOWN_START;
import static com.tpnet.retrofitdownloaddemo.download.DownInfo.DOWN_STOP;

/**
 * 下载状态
 * Created by litp on 2017/4/10.
 */

@IntDef({DOWN_START,DOWN_PAUSE,DOWN_STOP,DOWN_ERROR,DOWN_ING,DOWN_FINISH})
@Retention(RetentionPolicy.SOURCE)
public @interface DownState {
}
