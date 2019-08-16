package com.gaohui.nestedrecyclerview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import com.gaohui.nestedrecyclerview.adapter.MultiTypeAdapter

class CategoryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ChildRecyclerView(context, attrs, defStyleAttr) {

    private val mDataList = ArrayList<Any>()

    init {
        initRecyclerView()
        initLoadMore()

        initData()
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

                val needLoadMore = getLastVisibleItem(this@CategoryView) >=  getTotalItemCount(this@CategoryView) - 4
                if(needLoadMore) {
                    onLoadMore()
                }
            }
        })

    }

    fun getLastVisibleItem(childRecyclerView:RecyclerView): Int {
        val layoutManager = childRecyclerView.layoutManager
        return if (layoutManager != null && layoutManager is StaggeredGridLayoutManager) {
            val iArr = IntArray(2)
            layoutManager.findLastVisibleItemPositions(iArr)
            if (iArr[0] > iArr[1]) iArr[0] else iArr[1]
        } else  {
            -1
        }
    }

    fun getTotalItemCount(childRecyclerView:RecyclerView): Int {
        return childRecyclerView.adapter?.itemCount?:-1
    }

    private fun initData() {
        for (i in 0..10) {
            mDataList.add("default child item $i")
        }
        adapter?.notifyDataSetChanged()
    }

    private fun onLoadMore() {
        val loadMoreSize = 5
        for (i in 0..loadMoreSize) {
            mDataList.add("load more child item $i")
        }
        adapter?.notifyItemRangeChanged(mDataList.size-loadMoreSize,mDataList.size)
    }

}