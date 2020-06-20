package com.gaohui.nestedrecyclerview.java.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.gaohui.nestedrecyclerview.R;

public class SimpleTextViewHolder extends RecyclerView.ViewHolder {
    public TextView mTv;
    public SimpleTextViewHolder(@NonNull View itemView) {
        super(itemView);
        mTv = (TextView) itemView.findViewById(R.id.textView);
    }
}
