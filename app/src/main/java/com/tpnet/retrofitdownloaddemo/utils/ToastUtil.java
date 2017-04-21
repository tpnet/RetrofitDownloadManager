package com.tpnet.retrofitdownloaddemo.utils;

import android.widget.Toast;

import com.tpnet.retrofitdownloaddemo.BaseApplication;

/**
 * Created by litp on 2017/4/21.
 */

public class ToastUtil  {
    
    
    public static void show(String text){
        Toast.makeText(BaseApplication.getContext(),text,Toast.LENGTH_SHORT).show();
    }
    
    
    
}
