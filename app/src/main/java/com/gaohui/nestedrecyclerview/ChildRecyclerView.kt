package com.gaohui.nestedrecyclerview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import com.gaohui.nestedrecyclerview.helper.FlingHelper
import com.gaohui.nestedrecyclerview.utils.UIUtils

open class ChildRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RecyclerView(context, attrs, defStyleAttr)  {

    private val mFlingHelper = FlingHelper(context)

    private var mMaxDistance = 0

    private var mVelocityY = 0

    var isStartFling: Boolean = false
    var totalDy: Int = 0

    var mParentRecyclerView:ParentRecyclerView? = null

    init {
        mMaxDistance = mFlingHelper.getVelocityByDistance((UIUtils.getScreenHeight() * 4).toDouble())
        overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        initScrollListener()
    }

    private fun initScrollListener() {
        addOnScrollListener(object :OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(isStartFling) {
                    totalDy = 0
                    isStartFling = false
                }
                totalDy += dy
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    dispatchParentFling()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

    }

    private fun dispatchParentFling() {
        mParentRecyclerView =  findParentRecyclerView()
        mParentRecyclerView?.run {
            if(isScrollTop() && mVelocityY != 0) {
                //当前ChildRecyclerView已经滑动到顶部，且竖直方向加速度不为0,如果有多余的需要交由父RecyclerView继续fling
                val flingDistance = mFlingHelper.getSplineFlingDistance(mVelocityY)
                if(flingDistance > (Math.abs(this@ChildRecyclerView.totalDy))) {
                    fling(0,-mFlingHelper.getVelocityByDistance(flingDistance + totalDy))
                }
                totalDy = 0
                mVelocityY = 0
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(ev != null && ev.action == MotionEvent.ACTION_DOWN) {
            mVelocityY = 0
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        if(isAttachedToWindow.not()) return false
        val fling = super.fling(velocityX, velocityY)
        if(!fling || velocityY >= 0) {
            //fling为false表示加速度达不到fling的要求，将mVelocityY重置
            mVelocityY = 0
        } else {
            //正在进行fling
            isStartFling = true
            mVelocityY = velocityY
        }
        return fling
    }


    fun isScrollTop(): Boolean {
        //RecyclerView.canScrollVertically(-1)的值表示是否能向下滚动，false表示已经滚动到顶部
        return !canScrollVertically(-1)
    }

    private fun findParentRecyclerView():ParentRecyclerView? {
        var parentView = parent
        while ((parentView is ParentRecyclerView).not()) {
            parentView = parentView.parent
        }
        return parentView as? ParentRecyclerView
    }

}