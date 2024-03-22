package com.gaohui.nestedrecyclerview.update;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.gaohui.nestedrecyclerview.update.listener.AbsNestedRVOnScrollListener;
import com.gaohui.nestedrecyclerview.update.listener.SimpleNestedRVOnScrollListener;
import com.gaohui.nestedrecyclerview.update.viewpager.AbsViewPagerAdapter;

import java.lang.ref.WeakReference;

public class UpdatedNestedRecyclerView extends RecyclerView {

    private static final String TAG = "NestedRecyclerView";


    public AbsNestedRVOnScrollListener scrollListener = new SimpleNestedRVOnScrollListener();

    private WeakReference<UpdatedNestedRecyclerView> parentReference = new WeakReference<>(this); // 注意要判断不等于自己，否则容易进入死循环

    private WeakReference<UpdatedNestedRecyclerView> childReference = null; // 注意判空，默认没有子类

    private float lastDownY = -1;

    public boolean isScrollDown = true;

    public boolean oldScrollOrientation = true;

    private static boolean sIsInterceptNextClick = false;

    public boolean isReachBottomEdge = false;
    public boolean isReachTopEdge = true;


    public UpdatedNestedRecyclerView(@NonNull Context context) {
        this(context,null);
    }

    public UpdatedNestedRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        // listener放到设置时绑定
        setOnScrollListener(scrollListener);
    }

    public void initEnv(AbsNestedRVOnScrollListener listener) {
        setOnScrollListener(listener);
    }


    private void setOnScrollListener(AbsNestedRVOnScrollListener listener) {
        if (scrollListener != null) {
            removeOnScrollListener(scrollListener);
        }
        scrollListener = listener;
        addOnScrollListener(scrollListener);
    }

    public void updateParentRecyclerView(UpdatedNestedRecyclerView recyclerView) {
        parentReference = new WeakReference<>(recyclerView);
    }

    public UpdatedNestedRecyclerView getParentRecyclerView() {
        return parentReference == null ? null : parentReference.get();
    }

    public UpdatedNestedRecyclerView getChildRecyclerView() {
        return childReference == null ? null : childReference.get();
    }

    public boolean isScrollDown() {
        if (lastDownY >= 0) {
            return isScrollDown;
        } else {
            return oldScrollOrientation;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int event = e.getActionMasked();
        if (event == MotionEvent.ACTION_DOWN) {
            lastDownY = e.getY();
        } else if (event == MotionEvent.ACTION_MOVE) {
            if (lastDownY < 0) {
                lastDownY = e.getY();
            } else {
                isScrollDown = (e.getY() - lastDownY) <= 0;
            }
        } else if (event == MotionEvent.ACTION_UP || event == MotionEvent.ACTION_CANCEL) {
            if (lastDownY >= 0) {
                oldScrollOrientation = isScrollDown;
            }
            lastDownY = -1;
            isScrollDown = true;
        }

        boolean result = super.onInterceptTouchEvent(e);
        Log.d(TAG, "result=" + result + ", onInterceptTouchEvent, ev=" + e);
        return result;
    }

    private static boolean checkNeedInterceptClick(int event) {
        return sIsInterceptNextClick && event == MotionEvent.ACTION_UP;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int event = e.getActionMasked();
        if (event == MotionEvent.ACTION_DOWN) {
            lastDownY = e.getY();
            sIsInterceptNextClick = false;
        } else if (event == MotionEvent.ACTION_MOVE) {
            if (lastDownY < 0) {
                lastDownY = e.getY();
            } else {
                isScrollDown = (e.getY() - lastDownY) <= 0;
            }
        } else if (event == MotionEvent.ACTION_UP || event == MotionEvent.ACTION_CANCEL) {
            if (lastDownY >= 0) {
                oldScrollOrientation = isScrollDown;
            }
            lastDownY = -1;
            isScrollDown = true;

            if (checkNeedInterceptClick(event)) {
                sIsInterceptNextClick = false;
                if (isParent()) { //只做一边效果，防止动画导致奇怪的Fling
                    e.setAction(MotionEvent.ACTION_CANCEL);
                }
            }
        }

        boolean oldReachBottomState = this.isReachBottom();
        boolean oldReachTopState = this.isReachTop();

        boolean result = super.onTouchEvent(e);

        if (event == MotionEvent.ACTION_MOVE) {
            boolean parentReachBottom = this.isParent() && this.isScrollDown && !oldReachBottomState && this.isReachBottom();
            boolean childReachTop = !this.isParent() && !this.isScrollDown && !oldReachTopState && this.isReachTop();
            if (childReachTop || parentReachBottom) {
                reDispatchDownEvent(e);
            }
        }

//        if (IHPPullRefresh.ENABLE_TOUCH_DEBUG) {
//            LLog.d(IHPPullRefresh.TOUCH_TAG, "result=" + result + ", onTouchEvent, ev=" + e);
//        }
        return result;
    }

    private boolean isParent() {
        return this.getParentRecyclerView() == this;
    }


    public boolean isReachTop() {
        return isReachTopEdge;
    }

    public boolean isReachBottom() {
        return isReachBottomEdge;
    }

    protected void reDispatchDownEvent(MotionEvent motionEvent) {
        View parent = getParentRecyclerView();
        if (parent == null) {
            return;
        }
        int oldAction = motionEvent.getActionMasked();
        motionEvent.setAction(MotionEvent.ACTION_CANCEL);
        parent.dispatchTouchEvent(motionEvent);
        motionEvent.setAction(MotionEvent.ACTION_DOWN);
        parent.dispatchTouchEvent(motionEvent);
        sIsInterceptNextClick = true;
        motionEvent.setAction(oldAction);
    }

    @Override
    public boolean fling(final int velocityX, final int velocityY) {
        if (scrollListener == null) {
            throw new IllegalArgumentException("## Fetal Error: need initEnv for the NestedRVOnScrollListener...");
        }
        if (lastDownY < 0) {
            if (velocityY > 0) {
                isScrollDown = true;
                oldScrollOrientation = true;
            } else if (velocityY < 0) {
                isScrollDown = false;
                oldScrollOrientation = false;
            }
        }

        scrollListener.setVelocityY(velocityY);
        return super.fling(velocityX, velocityY);
    }
    /**
     * 获取最后一个item是recyclerView的 如果是且完全可见则说明父RecyclerView不能再滚动了，需要留给子RecyclerView滚动
     *
     * @return
     */
    @Nullable
    public UpdatedNestedRecyclerView getLastRecyclerView() {
        View view = getLastItem();

        if (view != null && view instanceof LinearLayout && ((LinearLayout) view).getChildAt(1) instanceof ViewPager) {
            ViewPager pager = (ViewPager) ((LinearLayout) view).getChildAt(1);
            if (pager != null && pager.getAdapter() instanceof AbsViewPagerAdapter) {
                return ((AbsViewPagerAdapter) pager.getAdapter()).getCurrentView();
            }
        }
        return null;
    }

    protected View getLastItem() {
        LayoutManager layoutManager = getLayoutManager();
        int position = -1;
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPosition = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
            position = lastPosition[0] > 0 ? lastPosition[0] : lastPosition[1];
            if (position == -1) {
                // 超过一屏高度的item，不识别
                lastPosition = staggeredGridLayoutManager.findLastVisibleItemPositions(null);
                position = lastPosition[0] > 0 ? lastPosition[0] : lastPosition[1];
            }
        } else if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            position = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            if (position == -1) {
                // 超过一屏高度的item，不识别
                position = linearLayoutManager.findLastVisibleItemPosition();
            }
        }
        if (position < 0) {
            return null;
        }

        ViewHolder holder = findViewHolderForLayoutPosition(position);
        return holder == null ? null : holder.itemView;
    }



}
