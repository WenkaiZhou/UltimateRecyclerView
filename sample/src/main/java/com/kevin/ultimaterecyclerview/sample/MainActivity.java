package com.kevin.ultimaterecyclerview.sample;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kevin.ultimaterecyclerview.UltimateRecyclerView;
import com.kevin.ultimaterecyclerview.pulltorefresh.PullToRefreshBase;
import com.kevin.ultimaterecyclerview.sample.adapter.HomeFunctionAdapter;
import com.kevin.ultimaterecyclerview.sample.bean.HomeFunction;
import com.kevin.ultimaterecyclerview.sample.util.LocalFileUtils;
import com.kevin.wraprecyclerview.WrapRecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    UltimateRecyclerView mUltimateRecyclerView;
    WrapRecyclerView mWrapRecyclerView;
    HomeFunctionAdapter mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUltimateRecyclerView = (UltimateRecyclerView) this.findViewById(R.id.main_act_urv);
        initRecyclerView();
        initEvent();
    }

    private void initEvent() {
        mUltimateRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<WrapRecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<WrapRecyclerView> refreshView) {
                new GetDataTask(true).execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<WrapRecyclerView> refreshView) {
                new GetDataTask(false).execute();
            }
        });
    }

    private class GetDataTask extends AsyncTask<Void, Void, Void> {

        boolean mIsPullDown;

        public GetDataTask(boolean isPullDown) {
            mIsPullDown = isPullDown;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Simulates a background job.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(mIsPullDown) {
                initRecyclerData();
            } else {
                String json = LocalFileUtils.getStringFormAsset(MainActivity.this, "homefunction1.json");
                List<HomeFunction> functionList = new Gson().fromJson(json, new TypeToken<List<HomeFunction>>() {
                }.getType());
                mAdapter.addToLast(functionList);
            }

            // Call onRefreshComplete when the list has been refreshed.
            mUltimateRecyclerView.onRefreshComplete();

            super.onPostExecute(result);
        }
    }

    /**
     * 初始化 RecyclerView
     *
     * @return void
     */
    private void initRecyclerView() {
        mWrapRecyclerView = mUltimateRecyclerView.getRefreshableView();

        mWrapRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mWrapRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        mWrapRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new HomeFunctionAdapter(this);
        mWrapRecyclerView.setAdapter(mAdapter);

        initRecyclerData();
    }

    /**
     * 初始化 RecyclerView数据
     */
    private void initRecyclerData() {
        String json = LocalFileUtils.getStringFormAsset(this, "homefunction.json");
        List<HomeFunction> functionList = new Gson().fromJson(json, new TypeToken<List<HomeFunction>>() {
        }.getType());
        mAdapter.setItemLists(functionList);
//        mWrapAdapter.notifyDataSetChanged();
    }
}