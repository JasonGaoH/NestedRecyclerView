package com.gaohui.nestedrecyclerview.update;

import android.content.Context;
import android.util.AttributeSet;

public class RecommendStaggeredGridLayoutManager extends SimpleStaggeredGridLayoutManager {
    public RecommendStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RecommendStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
    }
}
