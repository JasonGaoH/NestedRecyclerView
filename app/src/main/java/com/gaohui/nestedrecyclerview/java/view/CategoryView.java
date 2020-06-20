package com.gaohui.nestedrecyclerview.java.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.gaohui.nestedrecyclerview.java.adapter.MultiTypeAdapter;

import java.util.ArrayList;

public class CategoryView extends ChildRecyclerView implements OnUserVisibleChange {
    private ArrayList<Object> mDataList = new ArrayList<>();

    boolean hasLoadData = false;

    public CategoryView(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        initRecyclerView();
        initLoadMore();
    }

    private void initLoadMore() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                tryLoadMoreIfNeed();
            }


            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void tryLoadMoreIfNeed() {
        if(getAdapter() == null) return;
        LayoutManager layoutManager = getLayoutManager();
        int[] intArray;
        if(layoutManager instanceof StaggeredGridLayoutManager) {
            intArray = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(intArray);
            for (int value : intArray) {
                if (value >= getAdapter().getItemCount() - 4) {
                    if (loadMore()) return;
                    break;
                }
            }
        }
    }

    private boolean loadMore() {
        int loadMoreSize = 5;
        for(int i =0;i<loadMoreSize;i++) {
            mDataList.add("load more child item " + i);
        }
        if(getAdapter() != null) {
            getAdapter().notifyItemRangeChanged(mDataList.size()-loadMoreSize,mDataList.size());
        }
        return true;
    }

    private void initRecyclerView() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        setLayoutManager(staggeredGridLayoutManager);
        setAdapter(new MultiTypeAdapter(mDataList));
    }

    public CategoryView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoryView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onUserVisibleChange(boolean isVisibleToUser) {
        if(!hasLoadData && isVisibleToUser) {
            initData();
        }
    }

    private void initData() {
        hasLoadData = true;
        for(int i = 0;i<=10;i++) {
            mDataList.add("default child item " + i);
        }
        if(getAdapter() != null) {
            getAdapter().notifyDataSetChanged();
        }
    }
}
