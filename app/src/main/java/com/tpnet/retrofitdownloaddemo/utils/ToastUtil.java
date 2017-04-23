package com.tpnet.retrofitdownloaddemo.utils;

import android.widget.Toast;

import com.tpnet.retrofitdownloaddemo.BaseApplication;

/**
 * Created by litp on 2017/4/21.
 */

public class ToastUtil  {
    
    private static Toast toast;
    
    
    public static void show(String text){
        if(toast == null ){
            toast = Toast.makeText(BaseApplication.getContext(),text,Toast.LENGTH_SHORT);
        }else{
            toast.setText(text);
        }
        toast.show();
    }
    
    
    
}
