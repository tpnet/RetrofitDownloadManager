package com.tpnet.downmanager.utils;

import android.os.Environment;

import com.tpnet.downmanager.download.DownInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.ResponseBody;

/**
 * 文件操作工具类
 * Created by litp on 2017/4/10.
 */

public class FileUtil {

    /**
     * 写入文件
     *
     * @param file
     * @param info
     * @throws IOException
     */
    public static void writeFile(ResponseBody responseBody, File file, DownInfo info) throws IOException {

        createDirectory(file.getAbsolutePath(), true);


        long allLength = info.totalLength() <= 0 ? responseBody.contentLength() : info.totalLength();

        //文件通道形式，比文件流快1/3
        FileChannel channelOut = null;   //输出的通道

        //随机访问文件，可以指定断电续传起始位置
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
        channelOut = randomAccessFile.getChannel();

        MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE,
                info.downLength(), allLength - info.downLength());

        byte[] buffer = new byte[1024 * 8];  //8k
        int len;
        int record = 0;
        while ((len = responseBody.byteStream().read(buffer)) != -1) {
            mappedBuffer.put(buffer, 0, len);
            record += len;
        }

        responseBody.byteStream().close();
        channelOut.close();
        randomAccessFile.close();
    }

    public static boolean checkSDcard() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    public static String getRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String getExtensionName(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
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


}
