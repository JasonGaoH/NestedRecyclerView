package com.gaohui.nestedrecyclerview.kotlin.tab

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.gaohui.nestedrecyclerview.R
import com.gaohui.nestedrecyclerview.UIUtils
import kotlinx.android.synthetic.main.matrix_store_tab_view.view.*

/**
 * DynamicTabView
 *
 * @property title
 * @property descText
 *
 * @constructor
 * @param context
 */
class DynamicTabView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null,
                                               defStyleAttr: Int = 0, private val title: String = "",
                                               private val descText: String? = "", private val tabIcon: TabIcon? = null
) : RelativeLayout(context, attrs, defStyleAttr) {

    val MAX_CHANGE_HEIGHT = UIUtils.dp2px(20f)

    init {
        LayoutInflater.from(context).inflate(R.layout.matrix_store_tab_view, this, true)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setBackgroundColor(Color.TRANSPARENT)
        setPadding(0,0,0, UIUtils.dp2px(2f))
        gravity = Gravity.CENTER_HORIZONTAL
    }

    /**
     * init
     */
    fun setup() {
        if (isTabIconNullOrEmpty(tabIcon)) {
            tabTitle.visibility = View.VISIBLE
            tabImage.visibility = View.GONE
            tabTitle.text = title
        } else {
            tabTitle.visibility = View.GONE
            tabImage.visibility = View.VISIBLE
            //支持Image的自定义
//            tabImage.loadImage(tabIcon?.normal?:"")
        }
        if(descText.isNullOrEmpty()) {
            tabDesc.visibility = View.GONE
        } else {
            tabDesc.visibility = View.VISIBLE
        }

        tabDesc.text = descText
        divider.setBackgroundColor(Color.parseColor("#999999"))
    }

    private fun isTabIconNullOrEmpty(tabIcon: TabIcon?) =
        tabIcon == null || tabIcon.normal.isEmpty()

    /**
     * set is selected
     *
     * @param isSelect
     */
    fun setIsSelected(isSelect: Boolean) {
        if (isSelect) {
            if (isTabIconNullOrEmpty(tabIcon)) {
                tabTitle.setTextColor(Color.RED)
            }
            tabDesc.setTextColor(Color.parseColor("#666666"))
            tabDesc.setBackgroundResource(R.drawable.dynamic_tab_desc_selected_bg)
        } else {
            if (isTabIconNullOrEmpty(tabIcon)) {
                tabTitle.setTextColor(Color.parseColor("#333333"))
            }
            tabDesc.setTextColor(Color.parseColor("#666666"))
            tabDesc.setBackgroundResource(0)
        }
    }

    /**
     * 改变子标题的的高度（支持动画）
     *
     * @param progress
     */
    fun changeProgress(progress: Float) {
        tabDesc.alpha = progress
        tabDesc.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            addRule(BELOW,titleContainer.id)
            addRule(CENTER_HORIZONTAL)
            topMargin = UIUtils.dp2px(2f)
            height = (MAX_CHANGE_HEIGHT * progress).toInt()
        }
    }
}