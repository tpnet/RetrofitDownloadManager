package com.tpnet.downmanager.utils;

import android.widget.Toast;

import com.tpnet.downmanager.download.DownManager;


/**
 * Created by litp on 2017/4/21.
 */

public class ToastUtil  {
    
    private static Toast toast;
    
    
    public static void show(String text){
        if(toast == null ){
            toast = Toast.makeText(DownManager.getInstance().getContext(), text, Toast.LENGTH_SHORT);
        }else{
            toast.setText(text);
        }
        toast.show();
    }
    
    
    
}
