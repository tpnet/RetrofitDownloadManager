package com.tpnet.downmanager.download.db;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.util.Log;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.squareup.sqldelight.SqlDelightStatement;
import com.tpnet.downmanager.DownInfoModel;
import com.tpnet.downmanager.download.DownInfo;
import com.tpnet.downmanager.download.DownManager;
import com.tpnet.downmanager.download.DownState;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.tpnet.downmanager.download.DownInfo.DOWN_EXIST_MAPPER;
import static com.tpnet.downmanager.download.DownInfo.TOTALLENGTH_MAPPER;


/**
 * 数据库操作类，单例
 * Created by litp on 2017/4/10.
 */

public class DBUtil {

    private static DBUtil databaseUtil;

    //数据库操作类
    private BriteDatabase db;


    public DBUtil() {


        db = new SqlBrite.Builder().build().wrapDatabaseHelper(getHelper(DownManager.getInstance().getContext()), Schedulers.io());

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static DBUtil getInstance() {
        if (databaseUtil == null) {
            synchronized (DBUtil.class) {
                if (databaseUtil == null) {
                    databaseUtil = new DBUtil();
                }
            }
        }
        return databaseUtil;
    }


    public DBHelper getHelper(Context context) {
        int v;
        try {
            v = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            v = 1;
        }
        return new DBHelper(context, v);
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
     * 插入下载信息
     *
     * @param downInfo
     */
    public void insertDownInfo(DownInfo downInfo) {

        //插入下载信息
        DownInfo.InsertDowninfo insert = new DownInfoModel.InsertDowninfo(db.getWritableDatabase());
        insert.bind(downInfo.downUrl(), downInfo.downType(), downInfo.savePath(), downInfo.totalLength(), downInfo.downLength(), downInfo.downState(),
                downInfo.startTime(), downInfo.finishTime());

        insert.program.executeInsert();

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
                        Log.e("@@", "更新下载中长度:" + downLength);
                        DownInfo.UpdateDownLength update = new DownInfoModel.UpdateDownLength(db.getWritableDatabase());
                        update.bind(downLength, s);
                        return update.program.executeUpdateDelete();
                    }
                })
                .subscribe();

    }


    //更新下载状态
    public Integer updateState(@DownState int state, String url) {

        DownInfo.UpdateDownState update = new DownInfoModel.UpdateDownState(db.getWritableDatabase());
        update.bind(state, url);
        int row = update.program.executeUpdateDelete();
        Log.e("@@", "更新数据库下载状态" + state);
        return row;


    }

    /**
     * 更新开始下载的时间
     */
    public void updateStartTime(String downUrl) {

        Observable.just(downUrl)
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        DownInfoModel.UpdateDownStartTime update = new DownInfoModel.UpdateDownStartTime(db.getWritableDatabase());
                        update.bind(System.currentTimeMillis(), s);
                        update.program.executeUpdateDelete();
                    }
                });

    }


    /**
     * 更新完成下载的时间
     */
    public void updateFinishTime(String downUrl) {
        Observable.just(downUrl)
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        DownInfoModel.UpdateDownFinishTime update = new DownInfoModel.UpdateDownFinishTime(db.getWritableDatabase());
                        update.bind(System.currentTimeMillis(), s);
                        update.program.executeUpdateDelete();
                    }
                });

    }


    /**
     * 更新总长度,如果本来的总长度大于新的，就不更新了
     *
     * @param downUrl
     */
    public void updateTotalLength(final long totalLength, final String downUrl) {

        SqlDelightStatement sqlDelightStatement = DownInfo.FACTORY.selectTotalLength(downUrl);
        db.createQuery(DownInfo.TABLE_NAME, sqlDelightStatement.statement, sqlDelightStatement.args)
                .mapToOneOrDefault(new Func1<Cursor, Long>() {
                    @Override
                    public Long call(Cursor cursor) {
                        return TOTALLENGTH_MAPPER.map(cursor);
                    }
                }, 0L)
                .observeOn(Schedulers.computation())
                .filter(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long aLong) {

                        return aLong < totalLength;
                    }
                })
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        DownInfo.UpdateTotalLength update = new DownInfoModel.UpdateTotalLength(db.getWritableDatabase());
                        update.bind(totalLength, downUrl);
                        update.program.executeUpdateDelete();
                    }
                });

    }


    /**
     * 更新下载类型
     *
     * @param type
     * @param downUrl
     */
    public void updateDownType(final String type, final String downUrl) {
        Observable.just(downUrl)
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        DownInfoModel.UpdateDownType update = new DownInfoModel.UpdateDownType(db.getWritableDatabase());
                        update.bind(type, s);
                        update.program.executeUpdateDelete();
                    }
                });

    }


    /**
     * 更新下载信息
     *
     * @param downInfo
     * @return
     */
    public Integer updateDowninfo(DownInfo downInfo) {
        DownInfoModel.UpdateDowninfo updateDowninfo = new DownInfoModel.UpdateDowninfo(db.getWritableDatabase());
        updateDowninfo.bind(downInfo.savePath(), downInfo.totalLength(), downInfo.downLength(), downInfo.downState(), downInfo.startTime(), downInfo.finishTime(), downInfo.downUrl());
        return updateDowninfo.program.executeUpdateDelete();
    }

    /**
     * 获取保存的手机路径
     *
     * @param downUrl
     * @return
     */
    public Observable<String> getDownSavePath(String downUrl) {
        SqlDelightStatement sqlDelightStatement = DownInfo.FACTORY.selectDowninfoSavePath(downUrl);
        return db.createQuery(DownInfo.TABLE_NAME, sqlDelightStatement.statement, sqlDelightStatement.args)
                .mapToOneOrDefault(new Func1<Cursor, String>() {
                    @Override
                    public String call(Cursor cursor) {
                        return DOWN_EXIST_MAPPER.map(cursor);
                    }
                }, "");

    }


    /**
     * 判断下载长度和总长度是否一致
     *
     * @param downUrl
     */
    public Observable<Boolean> getDownLengthIsEqual(String downUrl) {
        SqlDelightStatement sqlDelightStatement = DownInfo.FACTORY.selctDownLengthIsEqual(downUrl);
        return db.createQuery(DownInfo.TABLE_NAME, sqlDelightStatement.statement, sqlDelightStatement.args)
                .mapToOne(new Func1<Cursor, Long>() {
                    @Override
                    public Long call(Cursor cursor) {
                        return DownInfo.LENGTHISEQUAL_MAPPER.map(cursor);
                    }
                })
                .map(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long aLong) {
                        return aLong > 0;
                    }
                });
    }
}
