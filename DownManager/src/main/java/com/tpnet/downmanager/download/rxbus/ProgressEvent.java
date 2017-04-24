package com.tpnet.downmanager.download.rxbus;

/**
 * Created by litp on 2017/4/22.
 */

public class ProgressEvent {
    
    
    long downLength;
    long totalLength;
    boolean isFinish;

    public ProgressEvent(long downLength, long totalLength, boolean isFinish) {
        this.downLength = downLength;
        this.totalLength = totalLength;
        this.isFinish = isFinish;
    }

    public long getDownLength() {
        return downLength;
    }

    public void setDownLength(long downLength) {
        this.downLength = downLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }
}
