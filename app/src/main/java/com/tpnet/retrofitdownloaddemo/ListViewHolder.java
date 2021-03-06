package com.tpnet.retrofitdownloaddemo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tpnet.downmanager.download.DownInfo;
import com.tpnet.downmanager.download.DownManager;
import com.tpnet.downmanager.download.listener.IOnDownloadListener;
import com.tpnet.downmanager.download.rxbus.Events;
import com.tpnet.downmanager.download.rxbus.RxBus;
import com.tpnet.downmanager.utils.ToastUtil;
import com.tpnet.retrofitdownloaddemo.utils.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.utils.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * 
 * Created by litp on 2017/4/18.
 */

class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Button mBtHandle;
    private TextView mTvName;
    private TextView mTvDownLength;
    private ProgressBar mPrbDown;

    private TextView mTvDownStartTime;
    private TextView mTvDownFinishTime;
    private TextView mTvState;


    private DownInfo downInfo;

    public ListViewHolder(View itemView) {
        super(itemView);
        mBtHandle = (Button) itemView.findViewById(R.id.bt_handle);
        mTvName = (TextView) itemView.findViewById(R.id.tv_name);
        mTvDownLength = (TextView) itemView.findViewById(R.id.tv_down_length);
        mPrbDown = (ProgressBar) itemView.findViewById(R.id.prb_down);
        mTvDownStartTime = (TextView) itemView.findViewById(R.id.tv_down_start_time);
        mTvDownFinishTime = (TextView) itemView.findViewById(R.id.tv_down_finish_time);
        mTvState = (TextView) itemView.findViewById(R.id.tv_state);


        mBtHandle.setOnClickListener(this);

        //因为(downInfo.downUrl()用来传递进度信息，这里使用两个(downInfo.downUrl()进行标识
        RxBus.with().setEvent(DownManager.DOWN_ADD_SUBSCRIBE)
                .onNext(new Action1<Events<?>>() {
                    @Override
                    public void call(Events<?> events) {
                        //添加监听器，在列表点击开始回调
                        String link = events.getContent();
                        link = link.replace(DownManager.DOWN_ADD_SUBSCRIBE, "");
                        Log.e("@@", "rxbus添加监听器");
                        DownManager.getInstance().addListener(link, listener);

                    }
                }).create();

    }


    public void setData(DownInfo data, int position) {

        this.downInfo = data;


        switch (downInfo.downState()) {
            case DownInfo.DOWN_ING:
                mBtHandle.setText("暂停");
                mTvState.setText("下载中..");
                DownManager.getInstance().startDown(downInfo);

                break;
            case DownInfo.DOWN_START:
                mBtHandle.setText("暂停");
                mTvState.setText("链接中...");
                DownManager.getInstance().startDown(downInfo);
                break;
            case DownInfo.DOWN_STOP:
                mTvState.setText("停止中");
                mBtHandle.setText("开始");
                break;
            case DownInfo.DOWN_PAUSE:
                mBtHandle.setText("开始");
                mTvState.setText("暂停中");
                break;
            case DownInfo.DOWN_ERROR:
                mBtHandle.setText("重试");
                mTvState.setText("出现错误");
                break;
            case DownInfo.DOWN_FINISH:
                mBtHandle.setText("打开");
                mTvState.setText("下载完成");

                mTvDownFinishTime.setVisibility(View.VISIBLE);
                //设置下载完成时间
                mTvDownFinishTime.setText(
                        String.format("完成时间: %s",
                                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(downInfo.finishTime())
                        )
                );

                break;

        }

        //恭喜View监听器
        DownManager.getInstance().addListener(downInfo.downUrl(), listener);

        if (TextUtils.isEmpty(downInfo.downName())) {
            //查询名字
            DatabaseUtil.getInstance().getName(downInfo.downUrl())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            mTvName.setText(s);
                        }
                    });
        } else {
            mTvName.setText(downInfo.downName());
        }
        


        //设置进度文本
        mTvDownLength.setText(
                String.format("%s/%s"
                        , FileUtil.getFormatSize(downInfo.downLength()), FileUtil.getFormatSize(downInfo.totalLength())));


        //计算进度
        if (downInfo.totalLength() == 0) {
            mPrbDown.setProgress(100);
        } else {
            mPrbDown.setProgress((int) (downInfo.downLength() * 100 / downInfo.totalLength()));
        }

        //设置开始下载时间
        if (downInfo.startTime() > 0) {
            mTvDownStartTime.setText(
                    String.format("开始时间: %s",
                            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(downInfo.startTime())
                    )
            );

        }


    }


    // 下载view回调
    IOnDownloadListener<DownInfo> listener = new IOnDownloadListener<DownInfo>() {
        @Override
        public void onNext(DownInfo baseDownEntity) {
            Log.e("@@", "listsner onNext下载完成");
            mBtHandle.setText("打开");
            mTvState.setText("下载完成");

            downInfo = DownInfo.create(downInfo)
                    .downState(DownInfo.DOWN_FINISH)
                    .build();

            mTvDownFinishTime.setVisibility(View.VISIBLE);
            //设置下载完成时间
            mTvDownFinishTime.setText(
                    String.format("完成时间: %s",
                            SimpleDateFormat.getInstance().format(new Date(downInfo.finishTime()))
                    )
            );

        }

        @Override
        public void onStart() {
            Log.e("@@", "listsner onStart开始下载");
            mBtHandle.setText("暂停");
            mTvState.setText("链接中...");

            downInfo = DownInfo.create(downInfo)
                    .downState(DownInfo.DOWN_START)
                    .build();
            mTvDownStartTime.setText(
                    String.format("开始时间: %s",
                            SimpleDateFormat.getInstance().format(new Date(downInfo.startTime()))
                    )
            );

        }

        @Override
        public void onComplete() {
            Log.e("@@", "listsner onComplete完成");
            //mBtHandle.setText("完成");


        }

        @Override
        public void onError(Throwable e) {
            Log.e("@@", "listsner onError下载错误");
            super.onError(e);
            mBtHandle.setText("重试");
            mTvState.setText("出现错误");

            downInfo = DownInfo.create(downInfo)
                    .downState(DownInfo.DOWN_ERROR)
                    .build();
        }


        @Override
        public void onPuase() {
            Log.e("@@", "listsner onPause下载暂停:" + downInfo.downState());
            super.onPuase();
            mBtHandle.setText("开始");
            mTvState.setText("暂停中");

            downInfo = DownInfo.create(downInfo)
                    .downState(DownInfo.DOWN_PAUSE)
                    .build();
        }

        @Override
        public void onStop() {
            Log.e("@@", "listsner onPause下载停止");
            super.onStop();
            mBtHandle.setText("开始");
            mTvState.setText("停止中");


            downInfo = DownInfo.create(downInfo)
                    .downState(DownInfo.DOWN_STOP)
                    .build();

        }

        @Override
        public void updateLength(long readLength, long totalLength, int percent) {

            //Log.e("@@", "listsner updateLength下载中:" + percent + " " + readLength + " " + totalLength);

            //设置文本
            mTvDownLength.setText(
                    String.format("%s/%s"
                            , FileUtil.getFormatSize(readLength), FileUtil.getFormatSize(totalLength)));

        }

        @Override
        public void updatePercent(int percent) {

            Log.e("@@", "listsner updatePercent更新进度:" + percent);


            mBtHandle.setText("暂停");
            mTvState.setText("下载中...");

            //计算进度
            mPrbDown.setProgress(percent);
        }
    };


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.bt_handle) {
            switch (downInfo.downState()) {
                case DownInfo.DOWN_ING:
                case DownInfo.DOWN_START:
                    //需要暂停
                    Log.e("@@", "点击了暂停");
                    DownManager.getInstance().pauseDown(downInfo);
                    break;
                case DownInfo.DOWN_STOP:
                case DownInfo.DOWN_PAUSE:
                case DownInfo.DOWN_ERROR:
                    //需要开始
                    Log.e("@@", "点击了 开始下载");
                    //需要设置监听器，
                    //downInfo.setListener(listener);
                    DownManager.getInstance().startDown(downInfo);

                    break;
                case DownInfo.DOWN_FINISH:
                    //需要打开
                    Log.e("@@", "点击了 完成");
                    if (FileUtil.getExtensionName(downInfo.savePath()).equals("apk")) {
                        //如果是安装包、
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(downInfo.savePath())),
                                "application/vnd.android.package-archive");
                        mBtHandle.getContext().startActivity(intent);
                    } else if (downInfo.downType().equals("application/octet-stream")) {

                        ToastUtil.show("文件类型: 二进制流，不知道文件类型。" + downInfo.downType());
                    } else {
                        ToastUtil.show("文件类型: " + downInfo.downType());
                    }

                    break;

            }
        }


    }


}
