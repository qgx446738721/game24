package voiddog.org.game24.adapter;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用适配器
 * Created by Dog on 2015/6/22.
 */
abstract public class CommonAdapter<T> extends BaseAdapter{
    public List<T> mDataList = new ArrayList<>();

    public void setDataList(List<T> dataList){
        this.mDataList = dataList;
        notifyDataSetChanged();
    }

    public void addDataList(int position, List<T> dataList){
        this.mDataList.addAll(position, dataList);
        notifyDataSetChanged();
    }

    public void addDataList(List<T> dataList){
        this.mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void addData(T data){
        this.mDataList.add(data);
        notifyDataSetChanged();
    }

    public void addData(int positoin, T data){
        this.mDataList.add(positoin, data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
