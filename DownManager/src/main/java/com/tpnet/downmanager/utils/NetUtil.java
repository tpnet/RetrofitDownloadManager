package com.tpnet.downmanager.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络工具类
 * Created by litp on 2017/4/10.
 */

public class NetUtil {

    /**
     * 匹配baseurl
     * 例如 https://www.baidu.com/asdasd/sadfas变为https://www.baidu.com/
     * @param url
     * @return
     */
    public static String getBasUrl(String url) {
        
        Pattern pattern = Pattern.compile("https*://\\S.*?/");
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()){
            return matcher.group();
        }else{
            throw new IllegalArgumentException("url地址错误");
        }
        
    }
}
