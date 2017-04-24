package com.tpnet.retrofitdownloaddemo.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.squareup.sqldelight.SqlDelightStatement;
import com.tpnet.retrofitdownloaddemo.BaseApplication;
import com.tpnet.retrofitdownloaddemo.Program;
import com.tpnet.retrofitdownloaddemo.ProgramModel;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * 数据库操作类，单例
 * Created by litp on 2017/4/10.
 */

public class DatabaseUtil {

    private static DatabaseUtil databaseUtil;

    //数据库操作类
    private BriteDatabase db;


    public DatabaseUtil() {

        db = new SqlBrite.Builder().build().wrapDatabaseHelper(getHelper(BaseApplication.getContext()), Schedulers.io());

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static DatabaseUtil getInstance() {
        if (databaseUtil == null) {
            synchronized (DatabaseUtil.class) {
                if (databaseUtil == null) {
                    databaseUtil = new DatabaseUtil();
                }
            }
        }
        return databaseUtil;
    }


    public DataBaseHelper getHelper(Context context) {
        int v;
        try {
            v = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            v = 1;
        }
        return new DataBaseHelper(context, v);
    }


    public Observable<String> getName(String downUrl) {

        SqlDelightStatement sqlDelightStatement = Program.FACTORY.selectDownName(downUrl);

        return db.createQuery(Program.TABLE_NAME, sqlDelightStatement.statement, sqlDelightStatement.args)
                .mapToOneOrDefault(new Func1<Cursor, String>() {
                    @Override
                    public String call(Cursor cursor) {
                        return Program.ROW_NAMW_MAPPER.map(cursor);
                    }
                },"获取失败");

    }



    /**
     * 插入下载bean
     *
     * @param program
     */
    public void insertProgrmm(Program program) {

        ProgramModel.InsertProgram insert = new ProgramModel.InsertProgram(db.getWritableDatabase());
        insert.bind(program.downLink(), program.name());
        insert.program.executeInsert();

    }



}
