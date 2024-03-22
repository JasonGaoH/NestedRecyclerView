package com.gaohui.nestedrecyclerview.update.listener;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gaohui.nestedrecyclerview.update.UpdatedNestedRecyclerView;
import com.gaohui.nestedrecyclerview.update.viewpager.AbsViewPagerAdapter;
import com.gaohui.nestedrecyclerview.update.viewpager.NestedViewPager;

public class TabNestedRVOnScrollListener extends SimpleNestedRVOnScrollListener {

    private boolean needReset = false;

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            UpdatedNestedRecyclerView parentRV = null;
            if (recyclerView instanceof UpdatedNestedRecyclerView) {
                parentRV = ((UpdatedNestedRecyclerView) recyclerView).getParentRecyclerView();

                // parent是ViewPager才处理！
                if (parentRV != null && recyclerView != parentRV && recyclerView.getParent() instanceof ViewPager) {
                    ViewPager viewPager = (ViewPager) recyclerView.getParent();

                    // 滚动停止时，保存一次位置
                    if (viewPager.getAdapter() instanceof AbsViewPagerAdapter) {
                        AbsViewPagerAdapter tabsAdapter = (AbsViewPagerAdapter) viewPager.getAdapter();
                        if (tabsAdapter.getTabItems() != null) {
                            tabsAdapter.saveRecyclerViewState((UpdatedNestedRecyclerView) recyclerView
                                    , tabsAdapter.getTabItems().get(viewPager.getCurrentItem()));
                        }
                    }
                }
            }
        }
    }

//    @Override
//    protected String getBehaviorScrollName(@NonNull RecyclerView recyclerView) {
//        if (recyclerView.getParent() instanceof ViewPager) {
//            ViewPager viewPager = (ViewPager) recyclerView.getParent();
//
//            if (viewPager.getAdapter() instanceof AbsViewPagerAdapter) {
//                AbsViewPagerAdapter tabsAdapter = (AbsViewPagerAdapter) viewPager.getAdapter();
//                int tabPosition = tabsAdapter.getCurrentPageTab();
//                String tabName = null;
//                if (tabsAdapter != null) {
//                    String item = tabsAdapter.getTabItem(tabPosition);
//                    if (item != null) {
//                        tabName = item;
//                    }
//                }
//                // 为了区分不同的Tab, 需要加上Tab名字, 和iOS对齐实现.
//                return SCROLL_EVENT_NAME + "-" + tabName;
//            }
//        }
//        return super.getBehaviorScrollName(recyclerView);
//    }

    @Override
    protected void onFlingChild(UpdatedNestedRecyclerView parentRV, RecyclerView recyclerView) {
        if (parentRV.getAdapter() == null || parentRV.getLayoutManager() == null) {
            return;
        }
        int position = parentRV.getAdapter().getItemCount() - 1;
        ViewGroup itemView = null;
        if (parentRV.getLayoutManager().findViewByPosition(position) instanceof ViewGroup) {
            itemView = (ViewGroup) parentRV.getLayoutManager().findViewByPosition(position);
        }
        if (itemView == null || itemView.getChildCount() <= 0) {
            return;
        }

        View view = itemView.getChildAt(1);
        if (view instanceof ViewPager && ((ViewPager) view).getAdapter() instanceof AbsViewPagerAdapter) {
            UpdatedNestedRecyclerView child = ((AbsViewPagerAdapter) ((ViewPager) view).getAdapter()).getCurrentView();
            if (child != null) {
                child.fling(0, (int) (velocityY * 0.5));
                // 用过就清空速度
                velocityY = 0;
            }
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (!(recyclerView instanceof UpdatedNestedRecyclerView)) {
            return;
        }
        UpdatedNestedRecyclerView parentRV = ((UpdatedNestedRecyclerView) recyclerView).getParentRecyclerView();
        if (parentRV == null) {
            return;
        }
        boolean isClear = false;
        // 父容器向上滚动，所有tab回顶，清空offset
        if (recyclerView == parentRV && (dy <= 0 || needReset)) {
            if (parentRV.getAdapter() == null) {
                return;
            }

            RecyclerView.ViewHolder viewHolder = parentRV.findViewHolderForAdapterPosition(parentRV.getAdapter().getItemCount() - 1);

            if (viewHolder != null && viewHolder.itemView instanceof LinearLayout
                    && ((LinearLayout) viewHolder.itemView).getChildCount() > 1
                    && ((LinearLayout) viewHolder.itemView).getChildAt(1) instanceof NestedViewPager) {
                NestedViewPager viewPager = (NestedViewPager) ((LinearLayout) viewHolder.itemView).getChildAt(1);
                if (viewPager != null && viewPager.getAdapter() != null) {
                    isClear = true;
                    needReset = false;
                    ((AbsViewPagerAdapter) viewPager.getAdapter()).clearAllOffset();
                }
            } else {
                // 这里viewholder已经被释放，所以需要记录状态，下次滚动到底部时重置
                needReset = true;
            }
        }
    }
}
