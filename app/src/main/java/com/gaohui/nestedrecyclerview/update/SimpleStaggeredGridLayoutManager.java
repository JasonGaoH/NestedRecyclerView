package com.gaohui.nestedrecyclerview.update;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

public class SimpleStaggeredGridLayoutManager extends StaggeredGridLayoutManager {
    private UpdatedNestedRecyclerView recyclerView;

    public void setRecyclerView(UpdatedNestedRecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public SimpleStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SimpleStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scroll = 0;
        try {
            // TODO: there is np exception from emas, but didn't find the root cause
            scroll = super.scrollVerticallyBy(dy, recycler, state);
            if (scroll == 0) {
                if (dy > 0) {
                    setReachEdge(null, true);
                } else if (dy < 0) {
                    setReachEdge(true, null);
                }
            } else if (dy != 0) {
                setReachEdge(false, false);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
//            Log.e(TAG, "scrollVerticallyBy " + throwable);
        }

        return scroll;
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
        setScrollToState(position);
    }

    @Override
    public void scrollToPositionWithOffset(int position, int offset) {
        super.scrollToPositionWithOffset(position, offset);
        setScrollToState(position);
    }

    private void setScrollToState(int targetPos) {
        if (targetPos == 0) {
            setReachEdge(true, false);
        } else if (recyclerView.getAdapter() != null
                && targetPos == recyclerView.getAdapter().getItemCount() - 1) {
            setReachEdge(false, true);
        } else {
            setReachEdge(false, false);
        }
    }

    private void setReachEdge(Boolean isReachTopEdge, Boolean isReachBottomEdge) {
        if (recyclerView == null || recyclerView.scrollListener == null) {
            return;
        }
        if (isReachTopEdge != null) {
            recyclerView.isReachTopEdge = isReachTopEdge;
        }
        if (isReachBottomEdge != null) {
            recyclerView.isReachBottomEdge = isReachBottomEdge;
        }
    }


}
