package com.gaohui.nestedrecyclerview.kotlin.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.gaohui.nestedrecyclerview.R

class SimpleTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val mTv: TextView = itemView.findViewById(R.id.textView) as TextView
}