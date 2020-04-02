package com.gaohui.nestedrecyclerview

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet

class StoreSwipeRefreshLayout : SwipeRefreshLayout {

    private var mParentRecyclerView:ParentRecyclerView? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mParentRecyclerView == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child is ParentRecyclerView) {
                    mParentRecyclerView = child
                    break
                }
            }
        }

    }

    override fun canChildScrollUp(): Boolean {
        return super.canChildScrollUp() || mParentRecyclerView?.isChildRecyclerViewCanScrollUp()?:false
    }
}