package com.gaohui.nestedrecyclerview.java.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.gaohui.nestedrecyclerview.UIUtils;
import com.gaohui.nestedrecyclerview.java.utils.FlingHelper;

public class ChildRecyclerView extends RecyclerView {
    private FlingHelper mFlingHelper;
    private int mMaxDistance = 0;
    private int mVelocity = 0;

    private boolean isStartFling = false;
    private int totalDy = 0;

    private ParentRecyclerView mParentRecyclerView = null;

    public ChildRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ChildRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChildRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mFlingHelper = new FlingHelper(context);
        mMaxDistance = mFlingHelper.getVelocityByDistance((double)(UIUtils.getScreenHeight() * 4));
        setOverScrollMode(OVER_SCROLL_NEVER);
        initScrollListener();
    }

    private void initScrollListener() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(newState == SCROLL_STATE_IDLE) {
                    dispatchParentFling();
                }
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(isStartFling) {
                    totalDy = 0;
                    isStartFling = false;
                }
                totalDy += dy;
            }
        });
    }

    private void dispatchParentFling() {
        mParentRecyclerView = findParentRecyclerView();
        if(isScrollTop() && mVelocity != 0) {
            //当前ChildRecyclerView已经滑动到顶部，且竖直方向加速度不为0,如果有多余的需要交由父RecyclerView继续fling
            double flingDistance = mFlingHelper.getSplineFlingDistance(mVelocity);
            if(flingDistance > (Math.abs(totalDy))) {
                mParentRecyclerView.fling(0,-mFlingHelper.getVelocityByDistance(flingDistance + totalDy));
            }
            totalDy = 0;
            mVelocity = 0;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev != null && ev.getAction() == MotionEvent.ACTION_DOWN) {
            mVelocity = 0;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if(!isAttachedToWindow()) return false;
        boolean fling = super.fling(velocityX, velocityY);
        if(!fling || velocityY >=0) {
            mVelocity =0;
        } else  {
            isStartFling = true;
            mVelocity = velocityY;
        }
        return fling;
    }

    boolean isScrollTop() {
        //RecyclerView.canScrollVertically(-1)的值表示是否能向下滚动，false表示已经滚动到顶部
        return !canScrollVertically(-1);

    }

    private ParentRecyclerView parentRecyclerView;
    private ParentRecyclerView findParentRecyclerView() {
        if(parentRecyclerView == null) {
            ViewParent parentView = getParent();
            while (!(parentView instanceof ParentRecyclerView)) {
                parentView = parentView.getParent();
            }
            parentRecyclerView = (ParentRecyclerView) parentView;
        }
        return parentRecyclerView;
    }
}
