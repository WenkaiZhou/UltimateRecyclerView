package com.kevin.ultimaterecyclerview.sample.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.LoadingLayoutBase;
import com.kevin.ultimaterecyclerview.sample.R;

/**
 * Created by zhouwk on 2015/12/30 0030.
 */
public class TmallFooterLayout extends FrameLayout {

    private FrameLayout mInnerLayout;
    private ImageView mCatImage;
    private TextView mDescText;
    private boolean hasMoreData = true;

    private AnimationDrawable animCat;

    public TmallFooterLayout(Context context) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.tmall_footer_loadinglayout, this);
        mInnerLayout = (FrameLayout) findViewById(R.id.fl_inner);
        mCatImage = (ImageView) mInnerLayout.findViewById(R.id.pull_to_refresh_cat);
        mDescText = (TextView) mInnerLayout.findViewById(R.id.pull_to_refresh_tv);

        LayoutParams lp = (LayoutParams) mInnerLayout.getLayoutParams();
        lp.gravity = Gravity.TOP;

        mCatImage.setImageResource(R.drawable.refreshing_footer_anim);
        animCat = (AnimationDrawable) mCatImage.getDrawable();

        hasMoreData = true;
        animCat.start();
    }


    public void setHasData() {
        if (animCat == null) {
            mCatImage.setImageResource(R.drawable.refreshing_footer_anim);
            animCat = (AnimationDrawable) mCatImage.getDrawable();
        }
        mDescText.setText("玩命加载中");
        hasMoreData = true;
        animCat.start();
    }

    public void setNoData() {
        if (animCat != null) {
            animCat.stop();
            animCat = null;
        }
        hasMoreData = false;
        mDescText.setText("喵，已经看到最后啦");
        mCatImage.setImageResource(R.mipmap.tm_load_cat_end);
    }

    public boolean isHasMoreData() {
        return hasMoreData;
    }
}
