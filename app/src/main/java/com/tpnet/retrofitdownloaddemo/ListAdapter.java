package com.tpnet.retrofitdownloaddemo;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tpnet.downmanager.download.DownInfo;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 * 
 * Created by litp on 2017/4/17.
 */

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> implements Action1<List<DownInfo>> {


    List<DownInfo> list = new ArrayList<>();


    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_down, parent, false));
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        //在Holder里面设置数据
        holder.setData(list.get(position), position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    
    //Rxjava查询回调
    @Override
    public void call(List<DownInfo> downInfos) {
        Log.e("@@","查询到列表数量"+downInfos.size());
        
        this.list = downInfos;
        notifyDataSetChanged();
    }




}
