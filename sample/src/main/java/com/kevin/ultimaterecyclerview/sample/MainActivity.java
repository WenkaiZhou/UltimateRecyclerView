package com.kevin.ultimaterecyclerview.sample;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kevin.loopview.AdLoopView;
import com.kevin.ultimaterecyclerview.UltimateRecyclerView;
import com.kevin.ultimaterecyclerview.sample.adapter.HomeProductAdapter;
import com.kevin.ultimaterecyclerview.sample.bean.HomeProduct;
import com.kevin.ultimaterecyclerview.sample.util.LocalFileUtils;
import com.kevin.ultimaterecyclerview.sample.view.TmallFooterLayout;
import com.kevin.ultimaterecyclerview.sample.view.TmallHeaderLayout;
import com.kevin.wraprecyclerview.WrapRecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    UltimateRecyclerView mUltimateRecyclerView;
    WrapRecyclerView mWrapRecyclerView;
    HomeProductAdapter mAdapter;
    TmallFooterLayout secondFooterLayout;
    // 顶部广告轮转大图
    AdLoopView mAdLoopView;
    Context mContext;

    int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();

        initViews();
        initEvent();
    }

    /**
     * 初始化View
     */
    private void initViews() {
        mUltimateRecyclerView = (UltimateRecyclerView) this.findViewById(R.id.main_act_urv);
        mUltimateRecyclerView.setHeaderLayout(new TmallHeaderLayout(this));
        secondFooterLayout = new TmallFooterLayout(this);
        mUltimateRecyclerView.setSecondFooterLayout(secondFooterLayout);

        initRecyclerView();

        new GetDataTask(false).execute();
    }

    /**
     * 初始化 RecyclerView
     *
     * @return void
     */
    private void initRecyclerView() {
        mWrapRecyclerView = mUltimateRecyclerView.getRefreshableView();
        mWrapRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        mWrapRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new HomeProductAdapter(this);
        mWrapRecyclerView.setAdapter(mAdapter);

        // 添加头部广告轮播
        initLoopView();

        // 添加头部功能选择，这里用一张图片模拟实现。
        ImageView functionImage = new ImageView(this);
        ViewGroup.LayoutParams functionParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        functionImage.setLayoutParams(functionParams);
        functionImage.setBackgroundResource(R.mipmap.tm_picture1);
        mWrapRecyclerView.addHeaderView(functionImage);
    }

    /**
     * 初始化LoopView
     *
     * 这里使用的是LoopView开源项目，项目地址：https://github.com/xuehuayous/Android-LoopView
     *
     * @return void
     */
    private void initLoopView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.recycler_header, null);
        mAdLoopView = (AdLoopView) layout.findViewById(R.id.main_act_alv);
        mWrapRecyclerView.addHeaderView(layout);

        // 初始化RotateView数据
        String json = LocalFileUtils.getStringFormAsset(this, "loopview.json");
        mAdLoopView.refreshData(json);
        mAdLoopView.startAutoLoop();
    }

    private void initEvent() {
        // 设置刷新监听
        mUltimateRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<WrapRecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<WrapRecyclerView> refreshView) {
                new GetDataTask(true).execute();
            }
        });

        // 设置最后一个条目可见监听
        mUltimateRecyclerView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                boolean hasMoreData = secondFooterLayout.isHasMoreData();
                Log.i("", "是否还有更多数据 " + hasMoreData);
                if(hasMoreData) {
                    new GetDataTask(false).execute();
                }
            }
        });
    }

    private class GetDataTask extends AsyncTask<Void, Void, Void> {

        boolean isRefresh;

        public GetDataTask(boolean isRefresh) {
            this.isRefresh = isRefresh;
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
            if(isRefresh) {
                page = 1;
            }
            Log.i("", "当前请求的是第 " + page + " 页数据");
            String json = LocalFileUtils.getStringFormAsset(MainActivity.this, "homeproduct"+ page +".json");
            List<HomeProduct> functionList = new Gson().fromJson(json, new TypeToken<List<HomeProduct>>() {
            }.getType());
            if(functionList.size() < 5) {
                // 每个分页加载数据少于5个,说明数据加载完成
                secondFooterLayout.setNoData();
            }

            if(isRefresh) { // 如果输刷新,重新设置数据
                secondFooterLayout.setHasData();
                mAdapter.setItemLists(functionList);
            } else {    // 如果加载更多,添加数据到尾部
                mAdapter.addToLast(functionList);
            }
            page++;

            // Call onRefreshComplete when the list has been refreshed.
            mUltimateRecyclerView.onRefreshComplete();

            super.onPostExecute(result);
        }
    }
}