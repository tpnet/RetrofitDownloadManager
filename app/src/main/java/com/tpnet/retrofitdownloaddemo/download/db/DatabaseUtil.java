package com.tpnet.retrofitdownloaddemo.download.db;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.util.Log;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.squareup.sqldelight.SqlDelightStatement;
import com.tpnet.retrofitdownloaddemo.BaseApplication;
import com.tpnet.retrofitdownloaddemo.DownInfoModel;
import com.tpnet.retrofitdownloaddemo.Program;
import com.tpnet.retrofitdownloaddemo.ProgramModel;
import com.tpnet.retrofitdownloaddemo.download.DownInfo;
import com.tpnet.retrofitdownloaddemo.download.DownState;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.tpnet.retrofitdownloaddemo.download.DownInfo.LIST_EXIST_MAPPER;

/**
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
     * 查询所有的下载
     *
     * @return
     */
    public Observable<List<DownInfo>> getAllDown() {
        SqlDelightStatement sqlDelightStatement = DownInfo.FACTORY.selectAll();
        return db.createQuery(DownInfo.TABLE_NAME, sqlDelightStatement.statement, sqlDelightStatement.args)
                .mapToList(new Func1<Cursor, DownInfo>() {
                    @Override
                    public DownInfo call(Cursor cursor) {
                        return DownInfo.LIST_ROW_MAPPER.map(cursor);
                    }
                });
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


    /**
     * 插入下载信息
     *
     * @param downInfo
     */
    public void insertDownInfo(DownInfo downInfo) {

        //插入下载信息
        DownInfo.InsertDowninfo insertDowninfo = new DownInfoModel.InsertDowninfo(db.getWritableDatabase());
        insertDowninfo.bind(downInfo.savePath(), downInfo.totalLength(), downInfo.downLength(), downInfo.downState(), downInfo.downUrl(),
                downInfo.startTime(), downInfo.finishTime());

        insertDowninfo.program.executeInsert();

    }


    /**
     * 更新下载进度
     *
     * @param downUrl
     */
    public void updateDownLength(final long downLength, final String downUrl) {
        Observable.just(downUrl)
                .observeOn(Schedulers.computation())
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        DownInfo.UpdateDownLength update = new DownInfoModel.UpdateDownLength(db.getWritableDatabase());
                        update.bind(downLength, s);
                        return update.program.executeUpdateDelete();
                    }
                })
                .subscribe();

    }


    //更新下载状态
    public Integer updateState(@DownState  int state,  String url) {

        DownInfo.UpdateDownState updateDownState = new DownInfoModel.UpdateDownState(db.getWritableDatabase());
        updateDownState.bind(state, url);
        int row = updateDownState.program.executeUpdateDelete();
        Log.e("@@", "更新状态" + state);
        return row;


    }


    /**
     * 更新总长度
     *
     * @param downUrl
     */
    public void updateTotalLength(final long totalLength, String downUrl) {
        Observable.just(downUrl)
                .observeOn(Schedulers.computation())
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        DownInfo.UpdateTotalLength update = new DownInfoModel.UpdateTotalLength(db.getWritableDatabase());
                        update.bind(totalLength, s);
                        return update.program.executeUpdateDelete();
                    }
                })
                .subscribe();

    }


    public Observable<String> getDownSavePath(String downUrl) {
        SqlDelightStatement sqlDelightStatement = DownInfo.FACTORY.selectDowninfoSavePath(downUrl);
        return db.createQuery(DownInfo.TABLE_NAME, sqlDelightStatement.statement, sqlDelightStatement.args)
                
                .mapToOneOrDefault(new Func1<Cursor, String>() {
                    @Override
                    public String call(Cursor cursor) {
                        return LIST_EXIST_MAPPER.map(cursor);
                    }
                },"")
                //失败返回空字符串
                /*.onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        return Observable.just("");
                    }
                })*/;

    }


}
