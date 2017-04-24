package com.tpnet.retrofitdownloaddemo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tpnet.retrofitdownloaddemo.download.DownInfo;
import com.tpnet.retrofitdownloaddemo.download.DownManager;
import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.download.listener.IOnDownloadListener;
import com.tpnet.retrofitdownloaddemo.utils.FileUtil;
import com.tpnet.retrofitdownloaddemo.utils.ToastUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by litp on 2017/4/18.
 */

class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Button mBtHandle;
    private TextView mTvName;
    private TextView mTvDownLength;
    private ProgressBar mPrbDown;

    private TextView mTvDownStartTime;
    private TextView mTvDownFinishTime;


    private DownInfo downInfo;

    public ListViewHolder(View itemView) {
        super(itemView);
        mBtHandle = (Button) itemView.findViewById(R.id.bt_handle);
        mTvName = (TextView) itemView.findViewById(R.id.tv_name);
        mTvDownLength = (TextView) itemView.findViewById(R.id.tv_down_length);
        mPrbDown = (ProgressBar) itemView.findViewById(R.id.prb_down);
        mTvDownStartTime = (TextView) itemView.findViewById(R.id.tv_down_start_time);
        mTvDownFinishTime = (TextView) itemView.findViewById(R.id.tv_down_finish_time);


        mBtHandle.setOnClickListener(this);

    }


    public void setData(DownInfo data, int position) {

        this.downInfo = data;


        switch (downInfo.downState()) {
            case DownInfo.DOWN_ING:
            case DownInfo.DOWN_START:
                mBtHandle.setText("暂停");
                DownManager.getInstance().startDown(downInfo);
                break;
            case DownInfo.DOWN_STOP:
            case DownInfo.DOWN_PAUSE:
                mBtHandle.setText("开始");

                break;
            case DownInfo.DOWN_ERROR:
                mBtHandle.setText("重试");
                break;
            case DownInfo.DOWN_FINISH:
                mBtHandle.setText("打开");

                mTvDownFinishTime.setVisibility(View.VISIBLE);
                //设置下载完成时间
                mTvDownFinishTime.setText(
                        String.format("完成时间: %s",
                                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(downInfo.finishTime())
                        )
                );

                break;

        }

        //添加view回调监听器,放在startDown后面，防止程序终止时候打开列表重新下载的问题
        DownManager.getInstance().addListener(data.downUrl(), listener);


        //查询名字
        DatabaseUtil.getInstance().getName(downInfo.downUrl())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mTvName.setText(s);
                    }
                });


        //设置进度文本
        mTvDownLength.setText(
                String.format("%s/%s"
                        , FileUtil.getFormatSize(data.downLength()), FileUtil.getFormatSize(data.totalLength())));


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


    /*下载回调*/
    IOnDownloadListener<DownInfo> listener = new IOnDownloadListener<DownInfo>() {
        @Override
        public void onNext(DownInfo baseDownEntity) {
            Log.e("@@", "listsner onNext下载完成");
            mBtHandle.setText("打开");

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

            downInfo = DownInfo.create(downInfo)
                    .downState(DownInfo.DOWN_ERROR)
                    .build();
        }


        @Override
        public void onPuase() {
            Log.e("@@", "listsner onPause下载暂停:" + downInfo.downState());
            super.onPuase();
            mBtHandle.setText("开始");

            downInfo = DownInfo.create(downInfo)
                    .downState(DownInfo.DOWN_PAUSE)
                    .build();
        }

        @Override
        public void onStop() {
            Log.e("@@", "listsner onPause下载停止");
            super.onStop();
            mBtHandle.setText("开始");


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
