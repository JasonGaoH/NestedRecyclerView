package com.gaohui.nestedrecyclerview.update.viewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.gaohui.nestedrecyclerview.update.RecommendStaggeredGridLayoutManager;
import com.gaohui.nestedrecyclerview.update.UpdatedNestedRecyclerView;
import com.gaohui.nestedrecyclerview.update.adpter.UpdatedMultiTypeAdapter;
import com.gaohui.nestedrecyclerview.update.listener.NestedRVOnScrollListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends AbsViewPagerAdapter {

    private SparseArray<UpdatedNestedRecyclerView> pages = new SparseArray<>(3);
    private LinkedList<Integer> positionList = new LinkedList<>();
    private Map<UpdatedNestedRecyclerView, Integer> realPosition = new HashMap<>(3);
    public int defaultPage = 0;

    private Map<String, Integer> scrollToPosition = new HashMap<>(32);
    private Map<String, Integer> offsets = new HashMap<>(32);

    private Context context;

//    private RecommendRepo recommendRepo;

    private ViewPager viewPager;

    private WeakReference<UpdatedNestedRecyclerView> parentReference;

    public List<String> tabItems = new ArrayList<>();

    private int startPosition = -1;
    private int currentPosition = -1;

    public ViewPagerAdapter(Context context, ViewPager pager) {
        this.context = context;
        viewPager = pager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                currentPageTab = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public synchronized void setTabItems(List<String> tabList) {
        if (tabList != null) {
            tabItems = tabList;
            // select default tab and the default one index is 0
            defaultPage = 0;
            notifyDataSetChanged();

            // add hp load track
//            Map<String, String> argsMap = new HashMap<>();
//            argsMap.put(SPMConstants.HOME_UT_PARAMS_JFY_DATA_FROM, LazDataPools.getInstance().getJfyRenderSourceType());
//            HpLoadTrackManager.getInstance().sendTrackEventWithDuration(SPMConstants.HOME_UT_EVENT_JFY_RENDER_FINISH,
//                    SPMConstants.HOME_UT_EVENT_JFY_CONTAINER_FIRST_DISPLAY, argsMap, true);
        }
    }

    public void setParentRecyclerView(UpdatedNestedRecyclerView parent) {
        parentReference = new WeakReference<>(parent);
    }

    @Override
    public UpdatedNestedRecyclerView getCurrentView() {
        return (viewPager == null) ? null : pages.get(getCommonIndex(viewPager.getCurrentItem()));

    }

    @Override
    public int getCurrentPageTab() {
        return currentPageTab;
    }

    @Override
    public void clearAllOffset() {
        scrollToPosition.clear();
        offsets.clear();
        for (UpdatedNestedRecyclerView recyclerView : realPosition.keySet()) {
            if (recyclerView != null) {
                recyclerView.scrollToPosition(0);
            }
        }
    }

    @Override
    public List<String> getTabItems() {
        return tabItems;
    }

    @Override
    public String getTabItem(int tabIndex) {
        if (tabItems == null || tabIndex < 0 || tabIndex >= tabItems.size()) {
            return null;
        }
        return tabItems.get(tabIndex);
    }

    @Override
    public void saveRecyclerViewState(UpdatedNestedRecyclerView recyclerView, String tabId) {

    }

    @Override
    public int getCount() {
        return tabItems.size();
    }

    private int getCommonIndex(int position) {
        return position % 5;
    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final String tabId = tabItems.get(position);
        int commonIndex = getCommonIndex(position);
        UpdatedNestedRecyclerView recyclerView = pages.get(getCommonIndex(position));
        final Integer posObj = Integer.valueOf(position);
        if (recyclerView == null) {
            recyclerView = createRecyclerView(position);
            adjustItemViewLayout(recyclerView);

            recyclerView.setClipToPadding(false);
            recyclerView.updateParentRecyclerView(parentReference == null ? null : parentReference.get());
            pages.put(commonIndex, recyclerView);
            realPosition.put(recyclerView, posObj);
        }

        // current cache view has contains the target and no need to rebind
        if (positionList.contains(posObj)) {
            return posObj;
        }

        Integer oldPosition = realPosition.get(recyclerView);
        // refresh real position of the recycler view
        realPosition.put(recyclerView, posObj);

        /** 需要对子view重排 */
        // remove reused view
        if (oldPosition != null) {
            positionList.remove(oldPosition);
        }

        // 剩余的view重新排列
        int insertIndex;
        int firstViewPos = positionList.size() > 0 ? positionList.get(0) : -1;
        int lastViewPos = positionList.size() > 1 ? positionList.get(1) : -1;

        if (position < firstViewPos) {
            // 比最小的位置小
            insertIndex = 0;
            positionList.add(0, posObj);
        } else if (position > firstViewPos && position < lastViewPos) {
            // 比最小的位置大，比最大的位置小
            insertIndex = 1;
            positionList.add(1, posObj);
        } else {
            // 比最大的位置大
            insertIndex = -1;
            positionList.add(posObj);
        }

        // 如果还没有destroy，需要手动destroy
        if (recyclerView.getParent() != null) {
            // 记录状态
            //越界问题保护
            if (oldPosition != null && oldPosition < tabItems.size()) {
                saveRecyclerViewState(recyclerView, tabItems.get(oldPosition));
            }
            ((ViewGroup) recyclerView.getParent()).removeView(recyclerView);
        }

        container.addView(recyclerView, insertIndex);
//        awesomeRequest(position, recyclerView);

//        setLoadMore(recyclerView, position);

        // 滚动到上次数据的位置
        final UpdatedNestedRecyclerView finalRecyclerView = recyclerView;
        ((StaggeredGridLayoutManager) finalRecyclerView.getLayoutManager()).scrollToPositionWithOffset(scrollToPosition.get(tabId) == null ? 0 : scrollToPosition.get(tabId), offsets.get(tabId) == null ? 0 : offsets.get(tabId));
        return posObj;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // do nothing
//        Log.d(TAG, "destroyItem position is: " + position);
    }

    private void adjustItemViewLayout(UpdatedNestedRecyclerView recyclerView) {

    }

    private UpdatedNestedRecyclerView createRecyclerView(int position) {
        final UpdatedNestedRecyclerView recyclerView = new UpdatedNestedRecyclerView(context);
        recyclerView.initEnv(new NestedRVOnScrollListener());
        RecommendStaggeredGridLayoutManager layoutManager = new RecommendStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setRecyclerView(recyclerView);
        //        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        if (position == 0) {
            //JFY首Tab才定制Item动画
//            recyclerView.setItemAnimator(new RecommendItemAnimator());
        }

        ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            recyclerView.setLayoutParams(layoutParams);
        } else {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        // ui 和 data 不需要sdk内的实现，所以用空实现覆盖
//        RecommendServer recommendServer = new RecommendServer.Builder((Activity) context, RecommendConst.SCENE_HOMEPAGE)
//                .registerDataSourceServer(new HomePageRecommendDataSourceServer())
//                .registerUiServer(new HomePageRecommendUiServer((Activity) context))
//                .registerInsertCardServer(new HomePageInsertCardServer())
//                .registerSmartClientServer(new HomePageSmartClientServer())
//                .build();
//        // create Just For You RecyclerView Adapter by lazada
//        JustForYouComponentMappingV4 jFYMapping = new JustForYouComponentMappingV4();
//        NestedRVAdapter adapter = new NestedRVAdapter(HomePageChameleonInfo.SCENE, recommendServer, jFYMapping, recyclerView, context);
//        // use load more adapter
//        RecyclerView.Adapter loadMoreAdapter = new LazLoadMoreAdapterV4(adapter);
        ArrayList<Object> mDataList = new ArrayList<>();

        for(int i = 0;i<=10;i++) {
            mDataList.add("default child item " + i);
        }
        UpdatedMultiTypeAdapter loadMoreAdapter = new UpdatedMultiTypeAdapter(mDataList);
        recyclerView.setAdapter(loadMoreAdapter);
        loadMoreAdapter.notifyDataSetChanged();

        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return recyclerView;
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (realPosition.get(view) == o);
    }
}
