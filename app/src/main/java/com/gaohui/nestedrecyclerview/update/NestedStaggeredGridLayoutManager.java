package com.gaohui.nestedrecyclerview.update;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

public class NestedStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

    @NonNull
    protected final UpdatedNestedRecyclerView mBindRecyclerView; // 当前layoutManger绑定的recyclerview

    public NestedStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes,@NonNull UpdatedNestedRecyclerView bindRecyclerView) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mBindRecyclerView = bindRecyclerView;
    }

    public NestedStaggeredGridLayoutManager(int spanCount, int orientation,@NonNull UpdatedNestedRecyclerView bindRecyclerView) {
        super(spanCount, orientation);
        mBindRecyclerView = bindRecyclerView;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
//        if (!isScrollEnabled) {
//            return 0;
//        }
//
        int scroll = 0;
        try {
            scroll = super.scrollVerticallyBy(dy, recycler, state);
//            if (isDisAbleCustomFunc()) {
//                return scroll;
//            }
        } catch (Exception e) {
//            sendCustomTrack("page_home", "/lz_home.home.nsgm_scroll_exception", null);
        }

        if (scroll == 0) {
            if (dy > 0) {
                mBindRecyclerView.isReachBottomEdge = true;
                mBindRecyclerView.isReachTopEdge = false;
            } else if (dy < 0) {
                mBindRecyclerView.isReachBottomEdge = false;
                mBindRecyclerView.isReachTopEdge = true;
            }
            if (mBindRecyclerView != null && mBindRecyclerView.scrollListener != null) {
                mBindRecyclerView.scrollListener.updatePullState(mBindRecyclerView);
            }
        } else if (dy != 0) {
            mBindRecyclerView.isReachBottomEdge = false;
            mBindRecyclerView.isReachTopEdge = false;
        }
        return scroll;
    }


    protected void childScrollToTop() {

    }
    @Override
    public void scrollToPosition(int position) {
        childScrollToTop();
        setScrollToState(position);
        super.scrollToPosition(position);
    }

    @Override
    public void scrollToPositionWithOffset(int position, int offset) {
        childScrollToTop();
        setScrollToState(position);
        super.scrollToPositionWithOffset(position, offset);
    }


    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        childScrollToTop();
        setScrollToState(position);
        super.smoothScrollToPosition(recyclerView, state, position);
    }

    private void setScrollToState(int targetPos) {
        if (mBindRecyclerView == null || mBindRecyclerView.scrollListener == null) {
            return;
        }

        if (targetPos == 0) {
            mBindRecyclerView.isReachTopEdge = true;
            mBindRecyclerView.isReachBottomEdge = false;
        } else if (targetPos == mBindRecyclerView.getAdapter().getItemCount() - 1) {
            mBindRecyclerView.isReachBottomEdge = true;
            mBindRecyclerView.isReachTopEdge = false;
        } else {
            mBindRecyclerView.isReachBottomEdge = false;
            mBindRecyclerView.isReachTopEdge = false;
        }
//        if (sDebug) {
//            Log.d(TAG, "setScrollToState " + " ,isReachBottomEdge: " + mBindRecyclerView.isReachBottomEdge + " ,isReachTopEdge: " + mBindRecyclerView.isReachTopEdge);
//        }
        mBindRecyclerView.scrollListener.updatePullState(mBindRecyclerView.getParentRecyclerView());
    }

    @Override
    public boolean canScrollVertically() {
        // TODO: add this, 禁止滑动
//        if (forbidScrollListener != null && forbidScrollListener.forbidScroll()) {
//            Log.i(TAG, "do not scroll vertical");
//            return false;
//        }

        // super always return true
        boolean superBoolean = super.canScrollVertically();
        if (mBindRecyclerView == null/* || isDisAbleCustomFunc()*/) {
            return superBoolean;
        }

        UpdatedNestedRecyclerView childView = mBindRecyclerView.getLastRecyclerView();
        if (childView == null) {
            return superBoolean;
        }

        // 手指下移时 最后一个item即猜你喜欢到达顶部且首页未到达顶部 则可以滚动
        boolean canScrollUp = (!mBindRecyclerView.isScrollDown && !mBindRecyclerView.isReachTopEdge && childView.isReachTopEdge);
        // 手指上移时 首页未到达底部或【首页已到达底部且猜你喜欢也到达底部】 则可以滚动
        boolean canScrollDown = (mBindRecyclerView.isScrollDown && !mBindRecyclerView.isReachBottomEdge);
        boolean canScroll = superBoolean && (canScrollUp || canScrollDown);

//        if (sDebug) {
//            Log.d(TAG, "canScrollVertically canScroll: " + canScroll + " ,isReachBottomEdge: " + mBindRecyclerView.isReachBottomEdge + " ,isReachTopEdge: "
//                    + mBindRecyclerView.isReachTopEdge + " ,childView.isReachTopEdge: " + childView.isReachTopEdge + " ,childView.isReachBottom: " + childView.isReachBottom());
//            Log.d(TAG, "canScrollVertically superBoolean: " + superBoolean + " ,canScrollUp: " + canScrollUp + " ,canScrollDown: " + canScrollDown);
//        }

        return canScroll/* && isScrollEnabled*/;
    }

}
