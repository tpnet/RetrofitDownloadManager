package com.tpnet.retrofitdownloaddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tpnet.retrofitdownloaddemo.download.db.DatabaseUtil;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import rx.android.schedulers.AndroidSchedulers;

/**
 * 
 * Created by litp on 2017/4/17.
 */

public class DownListActivity extends RxAppCompatActivity {
    
    
    private RecyclerView mRcvList;
    
    private ListAdapter adapter;
 
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_list);
        mRcvList = (RecyclerView) findViewById(R.id.rcv_list);
        
        mRcvList.setLayoutManager(new LinearLayoutManager(this));


        adapter = new ListAdapter();
        
        mRcvList.setAdapter(adapter);

        //查询当前所有的下载
        DatabaseUtil.getInstance().getAllDown()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter);
        

    }
    
    
    
    
}
