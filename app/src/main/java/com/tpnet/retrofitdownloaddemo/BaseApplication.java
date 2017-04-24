package com.tpnet.retrofitdownloaddemo;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import com.tpnet.downmanager.download.DownManager;

/**
 * 
 * Created by litp on 2017/4/19.
 */

public class BaseApplication extends Application {


    private static BaseApplication instance;

 

    public static Context getContext() {
        return instance.getApplicationContext();
        
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化下载器
        DownManager.init(this);
        
        instance = this;
        Stetho.initializeWithDefaults(this);
    }
}
