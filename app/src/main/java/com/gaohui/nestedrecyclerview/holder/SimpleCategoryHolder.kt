package com.gaohui.nestedrecyclerview.holder

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.gaohui.nestedrecyclerview.CategoryView
import com.gaohui.nestedrecyclerview.ChildRecyclerView
import com.gaohui.nestedrecyclerview.R
import com.gaohui.nestedrecyclerview.adapter.CategoryPagerAdapter
import com.gaohui.nestedrecyclerview.bean.CategoryBean

class SimpleCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val mTabLayout: TabLayout = itemView.findViewById(R.id.tabs) as TabLayout
    private val mViewPager: ViewPager = itemView.findViewById(R.id.viewPager) as ViewPager

    val viewList = ArrayList<ChildRecyclerView>()

    private var mCurrentRecyclerView :ChildRecyclerView? = null

    init {

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                if(viewList.isEmpty().not()) {
                    mCurrentRecyclerView = viewList[position]
                    Log.d("gaohui","onPageSelected: $position $mCurrentRecyclerView")
                }
            }
            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }

    fun bindData(obj: Any) {
        //TODO 需要优化，这里每次item被回收时都会重新setupWithViewPager
        (obj as? CategoryBean)?.apply {
            viewList.clear()
            tabTitleList.forEach{ _ ->
                val categoryView = CategoryView(itemView.context)
                viewList.add(categoryView)
            }
            mCurrentRecyclerView = viewList[mViewPager.currentItem]
            val lastItem = mViewPager.currentItem
            Log.d("gaohui","bindData: ${mViewPager.currentItem} $mCurrentRecyclerView")

            mViewPager.adapter = CategoryPagerAdapter(viewList,tabTitleList)
            mTabLayout.setupWithViewPager(mViewPager)
            mViewPager.currentItem = lastItem
        }
    }

    fun getCurrentChildRecyclerView(): ChildRecyclerView? {
        return mCurrentRecyclerView
    }
}