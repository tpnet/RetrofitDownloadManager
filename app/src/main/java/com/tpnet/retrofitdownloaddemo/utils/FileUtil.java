package com.tpnet.retrofitdownloaddemo.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * 文件操作工具类
 * Created by litp on 2017/4/10.
 */

public class FileUtil {

    public static boolean checkSDcard() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    public static String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }
    
    public static String getFileName(String path){
        return path.substring(path.lastIndexOf("/")+1);
    }
    
    public static String getExtensionName(String path){
        return path.substring(path.lastIndexOf(".")+1);
    }


    /**
     * 分级建立文件夹
     *
     * @param path      路径
     * @param isFileEnd 最后是否文件，还是文件夹
     */
    public static void createDirectory(String path, Boolean isFileEnd) {

        //对SDpath进行处理，分层级建立文件夹
        String[] s = path.split(File.separator);

        for (int i = 1; i < (isFileEnd ? s.length - 1 : s.length); i++) {

            File file = new File(getRootPath() + s[i]);
            if (!file.exists()) {
                file.mkdir();
            }
        }

    }

    /**
     * 创建文件，目录不存在会自动创建目录
     *
     * @param path
     * @param isFilend
     * @return
     */
    public static File createNewFile(String path, Boolean isFilend) {
        File newFile = new File(path);
        if (newFile.exists()) {
            return newFile;
        } else {
            createDirectory(path, isFilend);
            try {
                newFile.createNewFile();
                return newFile;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }



    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "B";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }
    
    
    public static Boolean  delFile(String filePath){
        File file  = new File(filePath);
        if(file.exists()){
            //如果是目录
            return file.delete();
        }
        return true;
    }
    
    
}
