package com.tpnet.retrofitdownloaddemo;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.tpnet.retrofitdownloaddemo.download.DownInfo;
import com.tpnet.retrofitdownloaddemo.download.DownManager;
import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.utils.FileUtil;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends RxAppCompatActivity implements View.OnClickListener {

    private ImageView mIvTest;


    private EditText mEtLinkOne;
    private Button mBtnAddOne;
    private EditText mEtLinkTwo;
    private Button mBtnAddTwo;
    private EditText mEtLinkThree;
    private Button mBtnAddThree;
    private Button mBtnList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mEtLinkOne = (EditText) findViewById(R.id.et_link_one);
        mBtnAddOne = (Button) findViewById(R.id.btn_add_one);
        mEtLinkTwo = (EditText) findViewById(R.id.et_link_two);
        mBtnAddTwo = (Button) findViewById(R.id.btn_add_two);
        mEtLinkThree = (EditText) findViewById(R.id.et_link_three);
        mBtnAddThree = (Button) findViewById(R.id.btn_add_three);
        mBtnList = (Button) findViewById(R.id.btn_list);

        mBtnAddOne.setOnClickListener(this);
        mBtnAddTwo.setOnClickListener(this);
        mBtnAddThree.setOnClickListener(this);
        mBtnList.setOnClickListener(this);
        
        


    }


    @Override
    public void onClick(View v) {
        if (v == mBtnAddOne) {


            down(mEtLinkOne.getText().toString(), "守护1");


        } else if (v == mBtnAddTwo) {

            down(mEtLinkTwo.getText().toString(), "守护2");

        } else if (v == mBtnAddThree) {

            down(mEtLinkThree.getText().toString(), "守护3");

        }
        if (v == mBtnList) {

            startActivity(new Intent(this, DownListActivity.class));
        }
    }


    private String getPath(String link) {

        File file = new File(FileUtil.getRootPath() + FileUtil.getFileName(link));

        if (file.exists()) {
            file.delete();
        }

        return file.getAbsolutePath();

    }





    //创建下载
    private void down(final String url, final String name) {
        //判断时候已经在下载列表了
        DatabaseUtil.getInstance().isDownExist(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            
                            //提示已经存在，是否重新下载
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("提示")
                                    .setMessage("已经存在该下载记录，是否覆盖?")
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startDown(url, name);
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();


                        } else {

                            startDown(url, name);

                        }
                    }
                });
    }
    
    
    //开始下载
    private void startDown(String url, String name) {

        //监听器可以在viewHolder里面设置
        DownInfo downInfo = DownInfo.builder()
                .savePath(getPath(url))
                .downUrl(url)
                .create();


        Program program = Program.create(url, name);

        //插入到下载bean
        DatabaseUtil.getInstance().insertProgrmm(program);

        //开始下载
        DownManager.getInstance().startDown(downInfo);
    }

}
