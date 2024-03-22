package com.gaohui.nestedrecyclerview.update.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

public class NestedViewPager extends ViewPager {
    private boolean canScroll = true;

    public NestedViewPager(Context context) {
        super(context);
    }


    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return canScroll && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return canScroll && super.onInterceptTouchEvent(ev);
    }
}
