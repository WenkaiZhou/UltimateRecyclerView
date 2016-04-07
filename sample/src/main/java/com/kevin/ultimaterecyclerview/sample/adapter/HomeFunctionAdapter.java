package com.kevin.ultimaterecyclerview.sample.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevin.ultimaterecyclerview.sample.bean.HomeFunction;
import com.kevin.ultimaterecyclerview.sample.R;
import com.kevin.ultimaterecyclerview.view.BaseRecyclerAdapter;

import java.util.LinkedList;

/**
 * 版权所有：xxx有限公司
 *
 * HomeFunctionAdapter
 *
 * @author zhou.wenkai ,Created on 2016-3-7 17:06:09
 * 		   Major Function：<b>首页功能适配器</b>
 *
 *         注:如果您修改了本类请填写以下内容作为记录，如非本人操作劳烦通知，谢谢！！！
 * @author mender，Modified Date Modify Content:
 */
public class HomeFunctionAdapter extends BaseRecyclerAdapter<HomeFunction, HomeFunctionAdapter.MyViewHolder> {

    public HomeFunctionAdapter(Context context) {
        super(context);
    }

    public HomeFunctionAdapter(Context mContext, LinkedList mItemLists) {
        super(mContext, mItemLists);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_home_function, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.descText.setText(mItemLists.get(position).getName());
        holder.position = position;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        View rootView;
        ImageView bgImage;
        TextView descText;
        int position;

        public MyViewHolder(View view) {
            super(view);
            rootView = view.findViewById(R.id.home_item_root);
            bgImage = (ImageView) view.findViewById(R.id.item_home_fun_image);
            descText = (TextView) view.findViewById(R.id.item_home_fun_name);

            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (null != onRecyclerViewListener) {
                onRecyclerViewListener.onItemClick(v, position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (null != onRecyclerViewListener) {
                return onRecyclerViewListener.onItemLongClick(position);
            }
            return false;
        }
    }
}
