package com.gaohui.nestedrecyclerview.update;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;


import java.util.List;

/**
 * @author tangmingjian
 * @version v1.0
 * @date 2022/10/25
 * @description 20221121 jfy tab抽象
 */
public interface IRecommendTabLayout {
    //-----------------------------------------------------------------
    // View相关方法 start
    //-----------------------------------------------------------------
    View getView();

    Context getContext();

    int getWidth();

    void setBackgroundColor(@ColorInt int color);

    ViewGroup.LayoutParams getLayoutParams();

    int getMeasuredHeight();

    View getChildAt(int index);

    void setLayoutParams(ViewGroup.LayoutParams params);

    void requestLayout();
    //-----------------------------------------------------------------
    // View相关方法 end
    //-----------------------------------------------------------------

    void setViewPager(ViewPager viewPager);

    void setIndicatorColor(int indicatorColor);

    void setFixTabFlag(boolean fixTabFlag);

    void updateTabStyles(List<String> list);

    void setLineDrawableEnabled(boolean enabled);

    void setJfyAtTop(boolean jfyAtTop);

    View getTabAt(int position);

    int getTabCount();

    void setOnLayoutListener(OnLayoutListener onLayoutListener);

    interface OnLayoutListener {
        void onScrollChanged(int scrollX);

        void onReachStart();

        void onReachEnd();

        void onWidthChanged(int width);
    }
}
