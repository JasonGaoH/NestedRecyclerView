package com.gaohui.nestedrecyclerview.ui

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.gaohui.nestedrecyclerview.R
import com.gaohui.nestedrecyclerview.adapter.MultiTypeAdapter
import com.gaohui.nestedrecyclerview.bean.CategoryBean
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val mDataList = ArrayList<Any>()

    private val strArray = arrayOf("推荐", "视频", "直播", "图片", "精华", "热门")

    var lastBackPressedTime = 0L

    private val multiTypeAdapter = MultiTypeAdapter(mDataList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        parentRecyclerView.initLayoutManager()
        parentRecyclerView.adapter = multiTypeAdapter

        refresh()

        swipeRefreshLayout.setColorSchemeColors(Color.RED)
        swipeRefreshLayout.setOnRefreshListener {
            refresh()
        }

    }

    private fun refresh() {
        mDataList.clear()
        for (i in 0..8) {
            mDataList.add("parent item text $i")
        }
        val categoryBean = CategoryBean()
        categoryBean.tabTitleList.clear()
        categoryBean.tabTitleList.addAll(strArray.asList())
        mDataList.add(categoryBean)
        multiTypeAdapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
            super.onBackPressed()
        } else {
            parentRecyclerView.scrollToPosition(0)
            Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_SHORT).show()
            lastBackPressedTime = System.currentTimeMillis()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        multiTypeAdapter.destroy()
    }
}
