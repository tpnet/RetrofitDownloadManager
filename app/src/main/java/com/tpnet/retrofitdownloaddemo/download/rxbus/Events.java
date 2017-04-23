package com.tpnet.retrofitdownloaddemo.download.rxbus;

/**
 * 
 * Created by litp on 2017/3/24.
 */

public class Events<T> {
 
 
    public String code;
    
    public T content;

    public static <O> Events<O> setContent(O t) {
        Events<O> events = new Events<>();
        events.content = t;
        return events;
    }

    public <T> T getContent() {
        return (T) content;
    }
    
    
}
