package com.gaohui.nestedrecyclerview.holder

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.ArrayMap
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

    val viewList = ArrayList<CategoryView>()

    var cacheVies = HashMap<String,CategoryView>()

    private var mCurrentRecyclerView :ChildRecyclerView? = null

    init {

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                if(viewList.isEmpty().not()) {
                    mCurrentRecyclerView = viewList[position]
                }
            }
            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }

    fun bindData(categoryBean: CategoryBean) {
        categoryBean.apply {
            viewList.clear()
            if(cacheVies.size > tabTitleList.size) {
                cacheVies.clear()
            }
            tabTitleList.forEach{
                var categoryView = cacheVies[it]
                if(categoryView == null || categoryView.parent != mViewPager) {
                    categoryView = CategoryView(itemView.context)
                    cacheVies[it] = categoryView
                }
                viewList.add(categoryView)
            }
            mCurrentRecyclerView = viewList[mViewPager.currentItem]
            val lastItem = mViewPager.currentItem

            mViewPager.adapter = CategoryPagerAdapter(viewList,tabTitleList)
            mTabLayout.setupWithViewPager(mViewPager)
            mViewPager.currentItem = lastItem
        }
    }

    fun getCurrentChildRecyclerView(): ChildRecyclerView? {
        return mCurrentRecyclerView
    }

    fun destroy() {
        cacheVies.clear()
    }
}