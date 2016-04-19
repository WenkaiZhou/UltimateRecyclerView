package com.kevin.ultimaterecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.handmark.pulltorefresh.library.LoadingLayoutBase;
import com.handmark.pulltorefresh.library.LoadingLayoutProxy;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.kevin.wraprecyclerview.WrapAdapter;
import com.kevin.wraprecyclerview.WrapRecyclerView;

import java.lang.reflect.Constructor;

/**
 * 版权所有：XXX有限公司
 *
 * UltimateRecyclerView
 *
 * @author zhou.wenkai  zwenkai@foxmail.com ,Created on 2016-4-7 21:57:10
 * Major Function：对PullToRefresh的扩展,增加支持RecyclerView
 *
 * 注:如果您修改了本类请填写以下内容作为记录，如非本人操作劳烦通知，谢谢！！！
 * @author mender，Modified Date Modify Content:
 */
public class UltimateRecyclerView extends PullToRefreshBase<WrapRecyclerView> {

    private LoadingLayoutBase mHeaderLoadingView;
    private LoadingLayoutBase mFooterLoadingView;

    private FrameLayout mSvHeaderLoadingFrame;
    private FrameLayout mSvFooterLoadingFrame;
    private FrameLayout mSvSecondFooterLoadingFrame;

    private boolean mURecyclerViewExtrasEnabled;

    private OnLastItemVisibleListener mOnLastItemVisibleListener;

    public UltimateRecyclerView(Context context) {
        super(context);
    }

    public UltimateRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public UltimateRecyclerView(Context context, Mode mode) {
        super(context, mode);
    }

    public UltimateRecyclerView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    /**
     * 获取刷新方向
     */
    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected void onRefreshing(final boolean doScroll) {
        /**
         * If we're not showing the Refreshing view, or the list is empty, the
         * the header/footer views won't show so we use the normal method.
         */
        WrapAdapter adapter = mRefreshableView.getAdapter();
        if (!mURecyclerViewExtrasEnabled || !getShowViewWhileRefreshing() || null == adapter || adapter.getItemCount() == 0) {
            super.onRefreshing(doScroll);
            return;
        }

        super.onRefreshing(false);

        final LoadingLayoutBase origLoadingView, recyclerViewLoadingView, oppositeRecyclerViewLoadingView;
        final int scrollToPosition, scrollToY;

        switch (getCurrentMode()) {
            case MANUAL_REFRESH_ONLY:
            case PULL_FROM_END:
                origLoadingView = getFooterLayout();
                recyclerViewLoadingView = mFooterLoadingView;
                oppositeRecyclerViewLoadingView = mHeaderLoadingView;
                scrollToPosition = mRefreshableView.getBottom();
                scrollToY = getScrollY() - getFooterSize();
                break;
            case PULL_FROM_START:
            default:
                origLoadingView = getHeaderLayout();
                recyclerViewLoadingView = mHeaderLoadingView;
                oppositeRecyclerViewLoadingView = mFooterLoadingView;
                scrollToPosition = mRefreshableView.getTop();
                scrollToY = getScrollY() + getHeaderSize();
                break;
        }

        // Hide our original Loading View
        origLoadingView.reset();
        origLoadingView.hideAllViews();

        // Make sure the opposite end is hidden too
        oppositeRecyclerViewLoadingView.setVisibility(View.GONE);

        // Show the RecyclerView Loading View and set it to refresh.
        recyclerViewLoadingView.setVisibility(View.VISIBLE);
        recyclerViewLoadingView.refreshing();

        if (doScroll) {
            // We need to disable the automatic visibility changes for now
            disableLoadingLayoutVisibilityChanges();

            // We scroll slightly so that the WrapRecyclerView's header/footer is at the
            // same Y position as our normal header/footer
            setHeaderScroll(scrollToY);

            // Make sure the RecyclerView is scrolled to show the loading
            // header/footer
            mRefreshableView.smoothScrollToPosition(scrollToPosition);

            // Smooth scroll as normal
            smoothScrollTo(0);
        }
    }

    @Override
    protected void onReset() {
        /**
         * If the extras are not enabled, just call up to super and return.
         */
        if (!mURecyclerViewExtrasEnabled) {
            super.onReset();
            return;
        }

        final LoadingLayoutBase originalLoadingLayout, recyclerViewLoadingLayout;
        final int scrollToHeight, selection;
        final boolean scrollSvToEdge;

        WrapAdapter adapter = mRefreshableView.getAdapter();

        switch (getCurrentMode()) {
            case MANUAL_REFRESH_ONLY:
            case PULL_FROM_END:
                originalLoadingLayout = getFooterLayout();
                recyclerViewLoadingLayout = mFooterLoadingView;
                selection = adapter.getItemCount() - 1;
                scrollToHeight = getFooterSize();
                scrollSvToEdge = Math.abs(getLastVisiblePosition() - selection) <= 1;
                break;
            case PULL_FROM_START:
            default:
                originalLoadingLayout = getHeaderLayout();
                recyclerViewLoadingLayout = mHeaderLoadingView;
                scrollToHeight = -getHeaderSize();
                selection = 0;
                scrollSvToEdge = Math.abs(getFirstVisiblePosition() - selection) <= 1;
                break;
        }

        // If the RecyclerView header loading layout is showing, then we need to
        // flip so that the original one is showing instead
        if (recyclerViewLoadingLayout.getVisibility() == View.VISIBLE) {

            // Set our Original View to Visible
            originalLoadingLayout.showInvisibleViews();

            // Hide the RecyclerView Header/Footer
            recyclerViewLoadingLayout.setVisibility(View.GONE);

            /**
             * Scroll so the View is at the same Y as the RecyclerView
             * header/footer, but only scroll if: we've pulled to refresh, it's
             * positioned correctly
             */
            if (scrollSvToEdge && getState() != State.MANUAL_REFRESHING) {
                mRefreshableView.scrollToPosition(selection);
                setHeaderScroll(scrollToHeight);
            }
        }

        // Finally, call up to super
        super.onReset();
    }

    @Override
    protected LoadingLayoutProxy createLoadingLayoutProxy(final boolean includeStart, final boolean includeEnd) {
        LoadingLayoutProxy proxy = super.createLoadingLayoutProxy(includeStart, includeEnd);

        if (mURecyclerViewExtrasEnabled) {
            final Mode mode = getMode();

            if (includeStart && mode.showHeaderLoadingLayout()) {
                proxy.addLayout(mHeaderLoadingView);
            }
            if (includeEnd && mode.showFooterLoadingLayout()) {
                proxy.addLayout(mFooterLoadingView);
            }
        }

        return proxy;
    }

    @Override
    protected WrapRecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        WrapRecyclerView recyclerView = new InternalWrapRecyclerView(context, attrs);
        recyclerView.setId(R.id.ultimate_recycler_view);
        return recyclerView;
    }

    private void init(Context context, AttributeSet attrs) {

        // Styleables from XML
        TypedArray ua = context.obtainStyledAttributes(attrs, R.styleable.UltimateRecyclerView);
        mURecyclerViewExtrasEnabled = ua.getBoolean(R.styleable.UltimateRecyclerView_ptrURecyclerViewExtrasEnabled, true);
        ua.recycle();

        // Styleables from XML
        TypedArray pa = context.obtainStyledAttributes(attrs, com.handmark.pulltorefresh.library.R.styleable.PullToRefresh);
        if (mURecyclerViewExtrasEnabled) {
            final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
            final ViewGroup.LayoutParams hlp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            // Create Loading Views ready for use later
            mSvHeaderLoadingFrame = new FrameLayout(getContext());
            mHeaderLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_START, pa);
            mHeaderLoadingView.setVisibility(View.GONE);
            mSvHeaderLoadingFrame.addView(mHeaderLoadingView, lp);
            mSvHeaderLoadingFrame.setLayoutParams(hlp);
            mRefreshableView.addHeaderView(mSvHeaderLoadingFrame);

            mSvFooterLoadingFrame = new FrameLayout(getContext());
            mFooterLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_END, pa);
            mFooterLoadingView.setVisibility(View.GONE);
            mSvFooterLoadingFrame.addView(mFooterLoadingView, lp);
            mSvFooterLoadingFrame.setLayoutParams(hlp);

            mSvSecondFooterLoadingFrame = new FrameLayout(getContext());
            mSvSecondFooterLoadingFrame.setLayoutParams(hlp);

            pa.recycle();

        }
    }

    @Override
    protected void handleStyledAttributes(TypedArray a) {
        super.handleStyledAttributes(a);
        /**
         * If the value for Scrolling While Refreshing hasn't been
         * explicitly set via XML, enable Scrolling While Refreshing.
         */
        if (!a.hasValue(com.handmark.pulltorefresh.library.R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
            setScrollingWhileRefreshingEnabled(true);
        }
    }

    @Override
    public void setHeaderLayout(LoadingLayoutBase headerLayout) {
        super.setHeaderLayout(headerLayout);

        try {
            Constructor c = headerLayout.getClass().getDeclaredConstructor(new Class[]{Context.class});
            LoadingLayoutBase mHeaderLayout = (LoadingLayoutBase)c.newInstance(new Object[]{getContext()});
            if(null != mHeaderLayout) {
                mSvHeaderLoadingFrame.removeAllViews();
                final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);

                mHeaderLoadingView = mHeaderLayout;
                mHeaderLoadingView.setVisibility(View.GONE);
                mSvHeaderLoadingFrame.addView(mHeaderLoadingView, lp);
                mRefreshableView.getAdapter().notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setFooterLayout(LoadingLayoutBase footerLayout) {
        super.setFooterLayout(footerLayout);

        try {
            Constructor c = footerLayout.getClass().getDeclaredConstructor(new Class[]{Context.class});
            LoadingLayoutBase mFooterLayout = (LoadingLayoutBase)c.newInstance(new Object[]{getContext()});
            if(null != mFooterLayout) {
                mSvFooterLoadingFrame.removeAllViews();
                final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);

                mFooterLoadingView = mFooterLayout;
                mFooterLoadingView.setVisibility(View.GONE);
                mSvFooterLoadingFrame.addView(mFooterLoadingView, lp);
                mRefreshableView.getAdapter().notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSecondFooterLayout(View secondFooterLayout) {
        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);

        mSvSecondFooterLoadingFrame.addView(secondFooterLayout, lp);
    }

    public final void setOnLastItemVisibleListener(OnLastItemVisibleListener listener) {
        mOnLastItemVisibleListener = listener;
    }

    @Override
    protected boolean isReadyForPullStart() {
        return isFirstItemVisible();
    }

    @Override
    protected boolean isReadyForPullEnd() {
        return isLastItemVisible();
    }

    /**
     * @Description: 判断第一个条目是否完全可见
     *
     * @return boolean:
     * @version 1.0
     * @date 2015-9-23
     * @Author zhou.wenkai
     */
    private boolean isFirstItemVisible() {
        final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();

        // 如果未设置Adapter或者Adapter没有数据可以下拉刷新
        if (null == adapter || adapter.getItemCount() == 0) {
            if (DEBUG) {
                Log.d(LOG_TAG, "isFirstItemVisible. Empty View.");
            }
            return true;
        } else {
            // 第一个条目完全展示,可以刷新
            if (getFirstVisiblePosition() == 0) {
                return mRefreshableView.getChildAt(0).getTop() >= mRefreshableView.getTop();
            }
        }
        return false;
    }

    /**
     * @Description: 获取第一个可见子View的位置下标
     *
     * @return int: 位置
     * @version 1.0
     * @date 2015-9-23
     * @Author zhou.wenkai
     */
    private int getFirstVisiblePosition() {
        View firstVisibleChild = mRefreshableView.getChildAt(0);
        return firstVisibleChild != null ? mRefreshableView
                .getChildAdapterPosition(firstVisibleChild) : -1;
    }

    /**
     * @Description: 判断最后一个条目是否完全可见
     *
     * @return boolean:
     * @version 1.0
     * @date 2015-9-23
     * @Author zhou.wenkai
     */
    private boolean isLastItemVisible() {
        final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();

        // 如果未设置Adapter或者Adapter没有数据可以上拉刷新
        if (null == adapter || adapter.getItemCount() == 0) {
            if (DEBUG) {
                Log.d(LOG_TAG, "isLastItemVisible. Empty View.");
            }
            return true;

        } else {
            // 最后一个条目View完全展示,可以刷新
            int lastVisiblePosition = getLastVisiblePosition();
            if(lastVisiblePosition >= mRefreshableView.getAdapter().getItemCount()-1) {
                return mRefreshableView.getChildAt(
                        mRefreshableView.getChildCount() - 1).getBottom() <= mRefreshableView
                        .getBottom();
            }
        }

        return false;
    }

    /**
     * @Description: 获取最后一个可见子View的位置下标
     *
     * @return int: 位置
     * @version 1.0
     * @date 2015-9-23
     * @Author zhou.wenkai
     */
    private int getLastVisiblePosition() {
        View lastVisibleChild = mRefreshableView.getChildAt(mRefreshableView
                .getChildCount() - 1);
        return lastVisibleChild != null ? mRefreshableView
                .getChildAdapterPosition(lastVisibleChild) : -1;
    }

    protected class InternalWrapRecyclerView extends WrapRecyclerView {

        private boolean mAddedSvFooter = false;
        private int mTmplastVisiblePosition;

        public InternalWrapRecyclerView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            try {
                super.dispatchDraw(canvas);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            try {
                return super.dispatchTouchEvent(ev);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void setAdapter(Adapter adapter) {
            // Add the Footer View at the last possible moment
            if (null != mSvFooterLoadingFrame && !mAddedSvFooter) {
                addFooterView(mSvSecondFooterLoadingFrame);
                addFooterView(mSvFooterLoadingFrame);
                mAddedSvFooter = true;
            }
            super.setAdapter(adapter);
        }

        @Override
        public void onScrolled(int dx, int dy) {
            super.onScrolled(dx, dy);
            if(null != mOnLastItemVisibleListener && isLastItemVisible()) {
                mOnLastItemVisibleListener.onLastItemVisible();
            }
        }

        /**
         * @Description: 判断最后一个条目是否能够可见
         *
         * @return boolean:
         * @version 1.0
         * @date 2016-4-12 14:51:04
         * @Author zhou.wenkai
         */
        private boolean isLastItemVisible() {
            final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();
            // 如果未设置Adapter,都没有添加自然不可见
            if(null == adapter) {
                return false;
            } else {
                // 最后一个条目View是否展示
                int lastVisiblePosition = getLastVisiblePosition();

                // 最后一个显示出来了
                if(lastVisiblePosition == mRefreshableView.getAdapter().getItemCount() - 2) {
                    // 说明最后一个刚刚显示出来
                    // 这里不希望和PullToRefreshListView中一样只要最后一个显示,每动一下就促发一次回调
                    if(lastVisiblePosition == mTmplastVisiblePosition + 1) {
                        mTmplastVisiblePosition = lastVisiblePosition;
                        return true;
                    }
                }
                mTmplastVisiblePosition = lastVisiblePosition;
            }
            return false;
        }

    }

}