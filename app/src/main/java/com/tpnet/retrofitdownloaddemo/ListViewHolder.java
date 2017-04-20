package com.tpnet.retrofitdownloaddemo;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tpnet.retrofitdownloaddemo.download.DownInfo;
import com.tpnet.retrofitdownloaddemo.download.DownManager;
import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.download.listener.IOnDownloadListener;
import com.tpnet.retrofitdownloaddemo.utils.FileUtil;

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

    private String name;
    
 
    private DownInfo downInfo;

    public ListViewHolder(View itemView) {
        super(itemView);
        mBtHandle = (Button) itemView.findViewById(R.id.bt_handle);
        mTvName = (TextView) itemView.findViewById(R.id.tv_name);
        mTvDownLength = (TextView) itemView.findViewById(R.id.tv_down_length);
        mPrbDown = (ProgressBar) itemView.findViewById(R.id.prb_down);
        
        
        
        mBtHandle.setOnClickListener(this);

    }


    public void setData(DownInfo data, int position) {
        
        this.downInfo  = data;

        //添加监听器
        downInfo.setListener(listener);

        switch (downInfo.downState()) {
            case DownInfo.DOWN_ING:
                mBtHandle.setText("暂停");
                
                break;
            case DownInfo.DOWN_STOP:
            case DownInfo.DOWN_PAUSE:
                mBtHandle.setText("开始");
                DownManager.getInstance().startDown(downInfo);
                break;
            case DownInfo.DOWN_ERROR:
                mBtHandle.setText("重试");
                break;
            case DownInfo.DOWN_FINISH:
                mBtHandle.setText("打开");
                break;

        }
        
        
        //查询名字
        DatabaseUtil.getInstance().getName(downInfo.downUrl())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mTvName.setText(s);
                        name = s;
                    }
                });

        //设置进度文本
        mTvDownLength.setText(
                String.format("%s/%s"
                        , FileUtil.getFormatSize(data.downLength()),FileUtil.getFormatSize(data.totalLength())));
        
        
        //计算进度
        if(downInfo.totalLength() == 0){
            mPrbDown.setProgress(100);
        }else{
            mPrbDown.setProgress((int)(downInfo.downLength() / downInfo.totalLength() * 100));
        }


    }



    /*下载回调*/
    IOnDownloadListener<DownInfo> listener = new IOnDownloadListener<DownInfo>() {
        @Override
        public void onNext(DownInfo baseDownEntity) {
            Log.e("@@","onNext下载完成");
            mBtHandle.setText("打开");
            Toast.makeText(mBtHandle.getContext(), name+" 下载完成", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStart() {
            Log.e("@@","listsner onStart开始下载");
            mBtHandle.setText("暂停");

        }

        @Override
        public void onComplete() {
            Log.e("@@","onComplete下载完成");
            mBtHandle.setText("打开");

        }

        @Override
        public void onError(Throwable e) {
            Log.e("@@","onError下载错误");
            super.onError(e);
            mBtHandle.setText("重试");

        }


        @Override
        public void onPuase() {
            Log.e("@@","onPause下载暂停");
            super.onPuase();
            mBtHandle.setText("开始");

        }

        @Override
        public void onStop() {
            Log.e("@@","onPause下载停止");
            super.onStop();
            mBtHandle.setText("开始");

        }

        @Override
        public void updateProgress(long readLength, long countLength,int percent) {
            Log.e("@@","onProgress下载中");

            mBtHandle.setText("暂停");


            //计算进度
            mPrbDown.setProgress(percent);

            //设置文本
            mTvDownLength.setText(
                    String.format("%s/%s"
                            , FileUtil.getFormatSize(readLength),FileUtil.getFormatSize(countLength)));

        }
    };


    @Override
    public void onClick(View v) {
        
        //downInfo.setListener(listener);

        if(v.getId() == R.id.bt_handle){
            switch (downInfo.downState()){
                case DownInfo.DOWN_ING:
                case DownInfo.DOWN_START:
                    //需要暂停
                    DownManager.getInstance().pauseDown(downInfo);
                    break;
                case DownInfo.DOWN_STOP:
                case DownInfo.DOWN_PAUSE:
                case DownInfo.DOWN_ERROR:
                    //需要开始
                    DownManager.getInstance().startDown(downInfo);
                    break;
                case DownInfo.DOWN_FINISH:
                    //需要打开
                    
                    break;
                 
            }
        }

    }
}
