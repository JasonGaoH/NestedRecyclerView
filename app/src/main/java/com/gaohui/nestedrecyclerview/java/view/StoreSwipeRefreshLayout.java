package com.gaohui.nestedrecyclerview.java.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

public class StoreSwipeRefreshLayout extends SwipeRefreshLayout {
    private ParentRecyclerView mParentRecyclerView = null;

    public StoreSwipeRefreshLayout(@NonNull Context context) {
        super(context);
    }

    public StoreSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mParentRecyclerView == null) {
            for(int i =0;i < getChildCount();i++) {
                View child = getChildAt(i);
                if(child instanceof ParentRecyclerView) {
                    mParentRecyclerView = (ParentRecyclerView) child;
                    break;
                }
            }
        }
    }

    @Override
    public boolean canChildScrollUp() {
        return super.canChildScrollUp() || (mParentRecyclerView !=null
                && mParentRecyclerView.isChildRecyclerViewCanScrollUp());
    }
}
