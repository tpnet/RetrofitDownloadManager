package com.tpnet.retrofitdownloaddemo.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tpnet.retrofitdownloaddemo.Program;

/**
 * Created by litp on 2017/4/12.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    //构造器，创建数据库
    public DataBaseHelper(Context context, int v) {

        this(context, "demo.db", null, v);
    }

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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

        //创建数据表
        db.execSQL(Program.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
