package com.gaohui.nestedrecyclerview.kotlin.holder

import android.graphics.Color
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.gaohui.nestedrecyclerview.kotlin.CategoryView
import com.gaohui.nestedrecyclerview.kotlin.ChildRecyclerView
import com.gaohui.nestedrecyclerview.R
import com.gaohui.nestedrecyclerview.kotlin.adapter.CategoryPagerAdapter
import com.gaohui.nestedrecyclerview.kotlin.bean.CategoryBean
import com.gaohui.nestedrecyclerview.kotlin.tab.DynamicTabLayout

class SimpleCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val mTabLayout: DynamicTabLayout = itemView.findViewById(R.id.newTabLayout) as DynamicTabLayout
    private val mViewPager: ViewPager = itemView.findViewById(R.id.viewPager) as ViewPager

    val viewList = ArrayList<CategoryView>()

    var cacheVies = HashMap<String, CategoryView>()

    private var mCurrentRecyclerView : ChildRecyclerView? = null

    private var isTabExpanded = true

    init {
        mTabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT)
        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                if(viewList.isEmpty().not()) {
                    mCurrentRecyclerView = viewList[position]
                    mCurrentRecyclerView?.apply {
                        addOnScrollListener(object :RecyclerView.OnScrollListener(){
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                super.onScrolled(recyclerView, dx, dy)
                                if(dy != 0) {
                                    dealWithChildScrollEvents(this@apply.isScrollTop())
                                }
                            }
                        })
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }

    private fun dealWithChildScrollEvents(scrollTop: Boolean) {
        if(isTabExpanded.not() && scrollTop) {
            mTabLayout.changeDescHeightWithAnimation(false)
            mTabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT)
            isTabExpanded = true
        } else if(isTabExpanded && scrollTop.not()) {
            mTabLayout.changeDescHeightWithAnimation(true)
            mTabLayout.setSelectedTabIndicatorColor(Color.RED)
            isTabExpanded = false
        }

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
                    categoryView =
                        CategoryView(itemView.context)
                    cacheVies[it] = categoryView
                }
                viewList.add(categoryView)
            }
            mCurrentRecyclerView = viewList[mViewPager.currentItem]
            val lastItem = mViewPager.currentItem

            mViewPager.adapter = CategoryPagerAdapter(
                viewList,
                tabTitleList
            )
            mTabLayout.setupWithViewPager(mViewPager)
            mViewPager.currentItem = lastItem
            //默认bind第一个子RecyclerView的滑动，不然第一个tab不会执行动画
            bindDefaultChildRecyclerViewScrolling(viewList[0])
        }
    }

    private fun bindDefaultChildRecyclerViewScrolling(categoryView: CategoryView) {
        categoryView.apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(dy != 0) {
                        dealWithChildScrollEvents(this@apply.isScrollTop())
                    }
                }
            })
        }
    }

    fun getCurrentChildRecyclerView(): ChildRecyclerView? {
        return mCurrentRecyclerView
    }

    fun destroy() {
        cacheVies.clear()
    }
}