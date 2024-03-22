package com.gaohui.nestedrecyclerview.update.viewpager;


import android.support.v4.view.PagerAdapter;

import com.gaohui.nestedrecyclerview.update.UpdatedNestedRecyclerView;

import java.util.List;

/**
 * Copyright (C) 2004 - 2023 Lazada Inc. All Rights Reserved.
 * Description : 下沉首页主RecyclerView的一些方法
 * <p>
 * Created by zukai.kzk@alibaba-inc.com on 2023/11/14
 */
public abstract class AbsViewPagerAdapter extends PagerAdapter {
    /**
     * NestedRVOnScrollListener使用到
     */
    public static int currentPageTab = 0;

    /**
     * NestedRecyclerView内部使用到
     */
    abstract public UpdatedNestedRecyclerView getCurrentView();

    abstract public int getCurrentPageTab();

    /**
     * NestedRVOnScrollListener使用到
     */
    abstract public void clearAllOffset();

    /**
     * NestedRVOnScrollListener使用到
     */
    abstract public List<String> getTabItems();

    /**
     * NestedRVOnScrollListener使用到
     */
    abstract public String getTabItem(int tabIndex);

    /**
     * NestedRVOnScrollListener使用到
     */
    abstract public void saveRecyclerViewState(UpdatedNestedRecyclerView recyclerView, String tabId);
}
