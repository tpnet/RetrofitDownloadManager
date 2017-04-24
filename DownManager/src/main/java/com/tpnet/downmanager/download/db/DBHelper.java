package com.tpnet.downmanager.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tpnet.downmanager.download.DownInfo;


/**
 * 
 * Created by litp on 2017/4/12.
 */

public class DBHelper extends SQLiteOpenHelper {

    //构造器，创建数据库
    public DBHelper(Context context, int v) {

        this(context, "tpdownload.db", null, v);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        
        //开启外键支持
        //db.execSQL("PRAGMA foreign_keys=ON");
    }

    //创建数据库的时候回调
    @Override
    public void onCreate(SQLiteDatabase db) {
        
        //创建下载信息表
        db.execSQL(DownInfo.CREATE_TABLE);
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
}
