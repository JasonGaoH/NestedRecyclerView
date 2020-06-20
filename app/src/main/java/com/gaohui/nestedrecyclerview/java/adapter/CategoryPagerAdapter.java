package com.gaohui.nestedrecyclerview.java.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;


import com.gaohui.nestedrecyclerview.java.view.CategoryView;

import java.util.ArrayList;

public class CategoryPagerAdapter extends PagerAdapter {

    private ArrayList<CategoryView> mViewList;
    private ArrayList<String> mTabList;

    private CategoryView mCurrentPrimaryItem = null;


    public CategoryPagerAdapter(ArrayList<CategoryView> viewList,ArrayList<String> tabList) {
        mViewList = viewList;
        mTabList = tabList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        CategoryView categoryView = mViewList.get(position);
        if(container == categoryView.getParent()) {
            container.removeView(categoryView);
        }
        container.addView(categoryView);
        return categoryView;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        CategoryView categoryView = (CategoryView) object;
        if(categoryView  != mCurrentPrimaryItem) {
            if(mCurrentPrimaryItem  != null) {
                mCurrentPrimaryItem.onUserVisibleChange(false);
            }
        }
        categoryView.onUserVisibleChange(true);
        mCurrentPrimaryItem = categoryView;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabList.get(position);
    }
}
