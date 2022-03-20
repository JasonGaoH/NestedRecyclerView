package com.gaohui.nestedrecyclerview.java.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.gaohui.nestedrecyclerview.UIUtils;
import com.gaohui.nestedrecyclerview.java.adapter.MultiTypeAdapter;
import com.gaohui.nestedrecyclerview.java.utils.FlingHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class ParentRecyclerView extends RecyclerView {
    FlingHelper mFlingHelper;

    int mMaxDistance = 0;
    /**
     * 记录当前滑动的y轴加速度
     */
    int velocityY = 0 ;
    /**
     * 记录上次Event事件的y坐标
     */
    Float lastY = 0f;
    int mTotalDy = 0;
    /**
     * 用于判断RecyclerView是否在fling
     */
    boolean isStartFling = false;

    AtomicBoolean canScrollVertically;
    private int mLastXInterceptX;
    private int mLastYInterceptY;

    private int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private void init(Context context) {
        mFlingHelper = new FlingHelper(context);
        mMaxDistance = mFlingHelper.getVelocityByDistance((double)(UIUtils.getScreenHeight() * 4));
        canScrollVertically = new AtomicBoolean(true);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //如果父RecyclerView fling过程中已经到底部，需要让子RecyclerView滑动神域的fling
                if(newState == SCROLL_STATE_IDLE) {
                    dispatchChildFling();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(isStartFling) {
                    mTotalDy = 0;
                    isStartFling = false;
                }
                //记录当前RecyclerView在y轴的偏移
                mTotalDy += dy;

            }
        });

    }

    public void initLayoutManager(Context context) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context) {
            @Override
            public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
                try {
                    return super.scrollVerticallyBy(dy, recycler, state);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            public void onLayoutChildren(Recycler recycler, State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean canScrollVertically() {
                ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
                return (canScrollVertically.get()  || childRecyclerView == null || childRecyclerView.isScrollTop());
            }

            @Override
            public void addDisappearingView(View child) {
                try {
                    super.addDisappearingView(child);
                } catch ( Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };

        layoutManager.setOrientation(VERTICAL);
        setLayoutManager(layoutManager);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean res = false;
        if(!isChildConsumeTouch(event)) {
            res = super.onInterceptHoverEvent(event);
        }
        return res;
    }



    private boolean isChildConsumeTouch(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if(event.getAction() !=  MotionEvent.ACTION_MOVE) {
            mLastXInterceptX = x;
            mLastYInterceptY = y;
            return false;
        }
        int deltaX = x - mLastXInterceptX;
        int deltaY = y - mLastYInterceptY;
        if(Math.abs(deltaX) <= Math.abs(deltaY) || Math.abs(deltaY) <= mTouchSlop) {
            return false;
        }
        return shouldChildScroll(deltaY);
    }

    private boolean shouldChildScroll(int deltaY) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        if(childRecyclerView == null) {
            return false;
        }
        if(isScrollToBottom()) {
            if(deltaY > 0) {
                return false;
            } else if(deltaY < 0 && !childRecyclerView.isScrollTop()) {
                return true;
            }
        } else {
            if(deltaY > 0 && !childRecyclerView.isScrollTop()) {
                return true;
            } else if(deltaY < 0) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev != null && ev.getAction() == MotionEvent.ACTION_DOWN) {
            //ACTION_DOWN的时候重置加速度
            velocityY = 0;
            stopScroll();
        }
        if(!(ev == null || ev.getAction() == MotionEvent.ACTION_MOVE)) {
            //在非ACTION_MOVE的情况下，将lastY置为0
            lastY = 0f;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(lastY == 0f) {
            lastY = e.getY();
        }
        if(isScrollToBottom()) {
            //如果父RecyclerView已经滑动到底部，需要让子RecyclerView滑动剩余的距离
            ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
            if(childRecyclerView != null) {
                int deltaY = (int) (lastY - e.getY());
                canScrollVertically.set(false);
                childRecyclerView.scrollBy(0,deltaY);
            }
        }
        if(e.getAction() == MotionEvent.ACTION_UP) {
            canScrollVertically.set(true);
        }
        lastY = e.getY();
        try {
            return super.onTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean fling(int velx, int velY) {
        boolean fling = super.fling(velx,velY);
        if(!fling || velY <= 0) {
            velocityY = 0;

        } else  {
            isStartFling = true;
            velocityY = velY;
        }
        return fling;
    }

    private void dispatchChildFling() {
        if(isScrollToBottom() && velocityY != 0) {
            double splineFlingDistance = mFlingHelper.getSplineFlingDistance(velocityY);
            if(splineFlingDistance > mTotalDy) {
                childFling(mFlingHelper.getVelocityByDistance(splineFlingDistance - mTotalDy));
            }
        }
        mTotalDy = 0;
        velocityY = 0;
    }

    private void childFling(int velocityByDistance) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        if(childRecyclerView != null) {
            childRecyclerView.fling(0,velocityByDistance);
        }
    }

    private ChildRecyclerView findNestedScrollingChildRecyclerView() {
        if(getAdapter()!= null && (getAdapter() instanceof MultiTypeAdapter)) {
            return ((MultiTypeAdapter)getAdapter()).getCurrentChildRecyclerView();
        }
        return  null;
    }

    private boolean isScrollToBottom() {
        //RecyclerView.canScrollVertically(1)的值表示是否能向上滚动，false表示已经滚动到底部
        return !canScrollVertically(1);
    }

    @Override
    public void scrollToPosition(final int position) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        if(childRecyclerView != null) {
            childRecyclerView.scrollToPosition(position);
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                ParentRecyclerView.super.scrollToPosition(position);
            }
        },50);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (target != null) && (target instanceof  ChildRecyclerView);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        //1.当前Parent RecyclerView没有滑动底，且dy> 0 是下滑
        boolean isParentCanScroll = dy > 0 && !isScrollToBottom();
        //2.当前Child RecyclerView滑到顶部了，且dy < 0,即上滑
        boolean isChildCanNotScroll = !(dy >= 0
                || childRecyclerView == null
                || !childRecyclerView.isScrollTop());
        //以上两种情况都需要让Parent RecyclerView去scroll，和下面onNestedPreFling机制类似
        if(isParentCanScroll || isChildCanNotScroll) {
            scrollBy(0,dy);
            consumed[1] = dy;
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return true;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        boolean isParentCanFling = velocityY > 0f && !isScrollToBottom();
        boolean isChildCanNotFling = !(velocityY >= 0
                || childRecyclerView == null
                || !childRecyclerView.isScrollTop());

        if(!isParentCanFling && !isChildCanNotFling) {
            return false;
        }
        fling(0,(int) velocityY);
        return true;
    }

    public boolean isChildRecyclerViewCanScrollUp() {
        ChildRecyclerView childRecyclerView = findNestedScrollingChildRecyclerView();
        if(childRecyclerView != null) {
            return  !childRecyclerView.isScrollTop();
        }
        return false;
    }

    public ParentRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ParentRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ParentRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

}
