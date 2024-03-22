package com.gaohui.nestedrecyclerview.update.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.gaohui.nestedrecyclerview.R;


public class UpdatedTextViewHolder extends RecyclerView.ViewHolder {
    public TextView mTv;
    public UpdatedTextViewHolder(@NonNull View itemView) {
        super(itemView);
        mTv = (TextView) itemView.findViewById(R.id.textView);
        StaggeredGridLayoutManager.LayoutParams slp;
        if ((itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams)) {
            slp = (StaggeredGridLayoutManager.LayoutParams)itemView.getLayoutParams();
            slp.setFullSpan(true);
        }

    }
}
