package com.tpnet.retrofitdownloaddemo;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.tpnet.retrofitdownloaddemo.download.DownInfo;
import com.tpnet.retrofitdownloaddemo.download.DownManager;
import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.tpnet.retrofitdownloaddemo.utils.FileUtil;
import com.tpnet.retrofitdownloaddemo.utils.ToastUtil;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity implements View.OnClickListener {

    private ImageView mIvTest;


    private EditText mEtLinkOne;
    private Button mBtnAddOne;
    private EditText mEtLinkTwo;
    private Button mBtnAddTwo;
 
    private Button mBtnList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mEtLinkOne = (EditText) findViewById(R.id.et_link_one);
        mBtnAddOne = (Button) findViewById(R.id.btn_add_one);
        mEtLinkTwo = (EditText) findViewById(R.id.et_link_two);
        mBtnAddTwo = (Button) findViewById(R.id.btn_add_two);
        mBtnList = (Button) findViewById(R.id.btn_list);

        mBtnAddOne.setOnClickListener(this);
        mBtnAddTwo.setOnClickListener(this);
        mBtnList.setOnClickListener(this);
        
        


    }


    @Override
    public void onClick(View v) {
        if (v == mBtnAddOne) {


            down(mEtLinkOne.getText().toString(), "ScreenToGif2.3.1.exe");


        } else if (v == mBtnAddTwo) {

            down(mEtLinkTwo.getText().toString(), "baiduyun单文件破解版.exe");

        }else if (v == mBtnList) {

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
        DatabaseUtil.getInstance().getDownSavePath(url)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(final String savePath) {
                        if (!TextUtils.isEmpty(savePath)) {
                            
                            showDialog(savePath,url,name);

                        } else {

                            startDown(url, name);

                        }
                    }
                });
    }
    
    
    private void showDialog(final String savePath,final String downUrl,final String name){
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
                    public void onClick(final DialogInterface dialog, int which) {
                        
                        dialog.dismiss();
                        showLoadingDialog();
                        
                        Observable.just(savePath)
                                .observeOn(Schedulers.computation())
                                .map(new Func1<String, Boolean>() {
                                    @Override
                                    public Boolean call(String s) {
                                        //删除原来的文件
                                        return FileUtil.delFile(s);
                                    }
                                })
                                .doAfterTerminate(new Action0() {
                                    @Override
                                    public void call() {
                                       
                                        progressDialog.dismiss();
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean aBoolean) {
                                        if(aBoolean){
                                            //删除成功开始下载
                                            startDown(downUrl, name);
                                        }else{
                                            //提示删除源文件失败
                                            ToastUtil.show("删除失败");
                                        }
                                    }
                                });
                  
                        
                    }
                })
                .show();
    }
    
    
    ProgressDialog progressDialog;
    
    
    private void showLoadingDialog(){
        if(progressDialog == null){
            progressDialog = ProgressDialog.show(this,"提示","正在删除");
        }else{
            progressDialog.show();
        }
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
