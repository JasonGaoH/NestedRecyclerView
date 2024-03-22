package com.gaohui.nestedrecyclerview.update.listener;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.gaohui.nestedrecyclerview.update.UpdatedNestedRecyclerView;

public class SimpleNestedRVOnScrollListener extends AbsNestedRVOnScrollListener {
    public static final String TAG = "Simple.NestedRVOnScroll";

    public static final String SCROLL_EVENT_NAME = "justForYouContainer";

    public float velocityY = 0;

    int oldState = RecyclerView.SCROLL_STATE_IDLE;

    private boolean isChecked = false;

    protected int viewPagerHeight = -1;



    protected String getBehaviorScrollName(@NonNull RecyclerView recyclerView) {
        return SCROLL_EVENT_NAME;


    }

    protected void onScrollStateChanged(int from, int state) {
        // nop
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        UpdatedNestedRecyclerView parentRV = null;
        UpdatedNestedRecyclerView currentRV = null;
        if (recyclerView instanceof UpdatedNestedRecyclerView) {
            currentRV = (UpdatedNestedRecyclerView) recyclerView;
            parentRV = ((UpdatedNestedRecyclerView) recyclerView).getParentRecyclerView();
        }
        if (recyclerView == parentRV) {
            onScrollStateChanged(IHPScrollListener.SCROLL_FROM_HP, newState);
        } else {
            onScrollStateChanged(IHPScrollListener.SCROLL_FROM_JFY, newState);
        }

        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            if (recyclerView.getParent() == null) {
                return;
            }

            if (parentRV == null) {
                return;
            }

            // 这里补偿fling传递
            if (oldState == RecyclerView.SCROLL_STATE_SETTLING && newState == RecyclerView.SCROLL_STATE_IDLE) {
                // 触发一次边界检查
                recyclerView.scrollBy(0, ((UpdatedNestedRecyclerView) recyclerView).isScrollDown() ? 1 : -1);
                flingWhere(parentRV, currentRV);
            }
            reInitChildRecyclerViewHeight(recyclerView, parentRV);
        }
        oldState = newState;
        super.onScrollStateChanged(recyclerView, newState);
    }

    protected void reInitChildRecyclerViewHeight(RecyclerView recyclerView, RecyclerView parentRV) {
        if (recyclerView != parentRV) {
            // 避免初始化高度不对，这里要再检查一次。只重复检查一次
            if (!isChecked) {
                isChecked = true;
                ViewGroup parentView = (ViewGroup) recyclerView.getParent().getParent();
                ViewGroup parent = (ViewGroup) recyclerView.getParent();
                int otherHeight = 0;
                for (int i = 0; i < parentView.getChildCount(); i++) {
                    View child = parentView.getChildAt(i);
                    if (child != parent) {
                        otherHeight += child == null ? 0 : child.getHeight();
                    }
                }
                if (otherHeight + parent.getHeight() != parentView.getHeight()) {
                    ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
                    layoutParams.height = parentView.getHeight() - otherHeight;
                    viewPagerHeight = layoutParams.height;
                    parent.setLayoutParams(layoutParams);
                }
            }
        }
    }


    private void flingWhere(UpdatedNestedRecyclerView parentRV, UpdatedNestedRecyclerView currentRV) {
        updatePullState(parentRV);
//        if (SDebug) {
        Log.d(TAG, "parentRV : " + parentRV);
        Log.d(TAG, "currentRV : " + currentRV);

        Log.d(TAG, "flingWhere parentRV.isReachBottom(): " + parentRV.isReachBottom() + " ,parentRV.isReachTop: " + parentRV.isReachTop()
                + " current.isReachTop: " + (currentRV).isReachTop() + " ,velocityY: " + velocityY);
//        }
        if (parentRV.isReachBottom()) {
            if (parentRV == currentRV) {
                // 父容器滚到底部了，需要传递fling事件
                if (parentRV.getAdapter() == null || parentRV.getLayoutManager() == null) {
                    return;
                }
                onFlingChild(parentRV, currentRV);
            } else {
                onFlingParent(parentRV, currentRV);
            }
        } else {
            onFlingParentWhenChildAppear(parentRV, currentRV);
        }
    }

    protected void onFlingParent(UpdatedNestedRecyclerView parentRV, UpdatedNestedRecyclerView currentRV) {
        // 子recyclerView滚到顶部了，需要传递fling事件到parent
        if ((currentRV).isReachTop() && velocityY != 0) {
            if (isSmoothScrollToParent()) {
                parentRV.isScrollDown = false; // 确保父容器的canScrollVertically能返回true
            }
            parentRV.fling(0, (int) (velocityY * 0.4));

            // 用过就清空速度
            velocityY = 0;
        }
    }

    protected void onFlingParentWhenChildAppear(UpdatedNestedRecyclerView parentRV, UpdatedNestedRecyclerView currentRV) {
    }

    /**
     * 传递fling给child
     */
    protected void onFlingChild(UpdatedNestedRecyclerView parentRV, RecyclerView recyclerView) {
        // 找到底部的recyclerView 传递一部分滚动事件
        UpdatedNestedRecyclerView child = parentRV.getLastRecyclerView();
        Log.d(TAG, "parent scroll to child : " + child + " ,velocityY: " + velocityY);
        if (child != null) {
            child.fling(0, (int) (velocityY * 0.5));
            // 用过就清空速度
            velocityY = 0;
        }
    }

    /**
     * 从JFY往上滑到父容器时，是否滑动到父容器：
     * true表示直接滑动到父容器
     * false表示先停留在JFY顶部，下次滑动才能滑到父容器
     */
    protected boolean isSmoothScrollToParent() {
        return true;
    }

    @Override
    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }

    @Override
    public void updatePullState(UpdatedNestedRecyclerView parentRV) {

    }
}
