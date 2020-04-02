package com.gaohui.nestedrecyclerview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import com.gaohui.nestedrecyclerview.adapter.MultiTypeAdapter

class CategoryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ChildRecyclerView(context, attrs, defStyleAttr),OnUserVisibleChange {

    private val mDataList = ArrayList<Any>()

    private var hasLoadData = false

    init {
        initRecyclerView()
        initLoadMore()
    }

    private fun initRecyclerView() {
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        layoutManager = staggeredGridLayoutManager
        adapter = MultiTypeAdapter(mDataList)
    }

    private fun initLoadMore() {
        addOnScrollListener(object :OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                tryLoadMoreIfNeed()
            }
        })

    }

    private fun tryLoadMoreIfNeed() {
        if(adapter == null) return
        val layoutManager = layoutManager
        val last: IntArray
        if (layoutManager is StaggeredGridLayoutManager) {
            last = IntArray(layoutManager.spanCount)
            layoutManager.findLastVisibleItemPositions(last)
            for (i in last.indices) {
                if ((last[i] >= adapter!!.itemCount - 4)) {
                    if (loadMore()) return
                    break
                }
            }
        }
    }

    private fun initData() {
        hasLoadData = true
        for (i in 0..10) {
            mDataList.add("default child item $i")
        }
        adapter?.notifyDataSetChanged()
    }

    private fun loadMore():Boolean {
        val loadMoreSize = 5
        for (i in 0..loadMoreSize) {
            mDataList.add("load more child item $i")
        }
        adapter?.notifyItemRangeChanged(mDataList.size-loadMoreSize,mDataList.size)
        return true
    }

    override fun onUserVisibleChange(isVisibleToUser: Boolean) {
        if(hasLoadData.not() && isVisibleToUser) {
            initData()
        }
    }

}