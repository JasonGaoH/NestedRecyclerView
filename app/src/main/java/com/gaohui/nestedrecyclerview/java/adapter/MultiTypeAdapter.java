package com.gaohui.nestedrecyclerview.java.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gaohui.nestedrecyclerview.R;
import com.gaohui.nestedrecyclerview.java.view.ChildRecyclerView;
import com.gaohui.nestedrecyclerview.java.viewholder.SimpleCategoryViewHolder;
import com.gaohui.nestedrecyclerview.java.viewholder.SimpleTextViewHolder;

import java.util.ArrayList;

public class MultiTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Object> mDataList;

    public static  final  int TYPE_TEXT = 0;
    public static  final  int TYPE_CATEGORY = 1;

    SimpleCategoryViewHolder mCategoryViewHolder;
    public MultiTypeAdapter(ArrayList<Object> dataList) {
        mDataList = dataList;
    }

    @Override
    public int getItemViewType(int position) {
        if(mDataList.get(position) instanceof String) {
            return TYPE_TEXT;
        } else  {
            return TYPE_CATEGORY;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == TYPE_TEXT) {
           return new SimpleTextViewHolder(LayoutInflater.from(
                    viewGroup.getContext()
            ).inflate(R.layout.layout_item_text, viewGroup, false));
        } else  {
            SimpleCategoryViewHolder simpleCategoryViewHolder =
                    new SimpleCategoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                            R.layout.layout_item_category_default,
                            viewGroup,
                            false
                    ));
            mCategoryViewHolder = simpleCategoryViewHolder;
            return simpleCategoryViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int pos) {
        if(viewHolder instanceof SimpleTextViewHolder) {
//            Log.d("gaohui","pos " + pos + mDataList.get(pos) + mDataList.get(pos));
            ( (SimpleTextViewHolder)viewHolder).mTv.setText((String)mDataList.get(pos));
        } else if(viewHolder instanceof SimpleCategoryViewHolder) {
            ((SimpleCategoryViewHolder)viewHolder).bindData(mDataList.get(pos));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public ChildRecyclerView getCurrentChildRecyclerView() {
        if(mCategoryViewHolder != null) {
            return mCategoryViewHolder.getCurrentChildRecyclerView();
        }
        return null;
    }

    public void destroy() {
        if(mCategoryViewHolder != null) {
            mCategoryViewHolder.destroy();
        }

    }
}
