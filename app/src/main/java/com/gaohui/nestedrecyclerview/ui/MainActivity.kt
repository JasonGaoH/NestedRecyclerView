package com.gaohui.nestedrecyclerview.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gaohui.nestedrecyclerview.R
import com.gaohui.nestedrecyclerview.adapter.MultiTypeAdapter
import com.gaohui.nestedrecyclerview.bean.CategoryBean
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val mDataList = ArrayList<Any>()

    private val strArray = arrayOf("关注", "推荐", "视频", "直播", "图片", "段子", "精华", "热门")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        parentRecyclerView.initLayoutManager()

        initData()
    }

    private fun initData() {
        val multiTypeAdapter = MultiTypeAdapter(mDataList)
        for (i in 0..8) {
            mDataList.add("parent item text $i")
        }
        val categoryBean = CategoryBean()
        categoryBean.tabTitleList.clear()
        categoryBean.tabTitleList.addAll(strArray.asList())
        mDataList.add(categoryBean)
        parentRecyclerView.adapter = multiTypeAdapter
        multiTypeAdapter.notifyDataSetChanged()
    }
}
