package com.gaohui.nestedrecyclerview.update.adpter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gaohui.nestedrecyclerview.R;
import com.gaohui.nestedrecyclerview.update.IRecommendTabLayout;
import com.gaohui.nestedrecyclerview.update.SlidingTabLayoutRevamp;
import com.gaohui.nestedrecyclerview.update.UpdatedNestedRecyclerView;
import com.gaohui.nestedrecyclerview.update.viewholder.UpdatedCategoryViewHolder;
import com.gaohui.nestedrecyclerview.update.viewholder.UpdatedTextViewHolder;
import com.gaohui.nestedrecyclerview.update.viewpager.NestedViewPager;
import com.gaohui.nestedrecyclerview.update.viewpager.ViewPagerAdapter;
import com.gaohui.nestedrecyclerview.update.utils.ScreenUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class UpdatedMultiTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Object> mDataList;

    public static  final  int TYPE_TEXT = 0;
    public static  final  int TYPE_CATEGORY = 1;

    public static final int RECOMMEND_TAB_HEIGHT = 68;

    private static WeakReference<NestedViewPager> sViewPagerRef;



//    UpdatedCategoryViewHolder mCategoryViewHolder;
    public UpdatedMultiTypeAdapter(ArrayList<Object> dataList) {
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
            return new UpdatedTextViewHolder(LayoutInflater.from(
                    viewGroup.getContext()
            ).inflate(R.layout.layout_item_text, viewGroup, false));
        } else  {
            UpdatedCategoryViewHolder simpleCategoryViewHolder =
                    new UpdatedCategoryViewHolder(createView(viewGroup,viewGroup.getContext()));
//            mCategoryViewHolder = simpleCategoryViewHolder;
            return simpleCategoryViewHolder;
        }
    }

    String[] strArray = new String[]{"推荐", "视频", "直播", "图片", "精华", "热门"};

    private View createView(View parent, @NonNull Context context) {
        final int targetHeight = parent == null ? context.getResources().getDisplayMetrics().heightPixels : parent.getHeight();
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

//        final RecommendRepo recommendRepo = getRepo();
        final IRecommendTabLayout slidingTabLayoutRevamp;
        final int tabHeight;
//        if (LazDataPools.getInstance().isHomeVersionV7()) {
//            tabHeight = ScreenUtils.dp2px(context, RECOMMEND_TAB_HEIGHT_NEW);
//            slidingTabLayoutRevamp = new RecommendTabLayout(context);
//        } else {
        tabHeight = ScreenUtils.dp2px(context, RECOMMEND_TAB_HEIGHT);
        slidingTabLayoutRevamp = new SlidingTabLayoutRevamp(context);
//        }

        FrameLayout frameLayout = new FrameLayout(context);
        layout.addView(frameLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, tabHeight));

        slidingTabLayoutRevamp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.addView((slidingTabLayoutRevamp.getView()));

        // add ViewPager
        final NestedViewPager viewPager = new NestedViewPager(context);
        viewPager.setId(R.id.id_guess_view_pager);
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewPager.setNestedScrollingEnabled(false);
        }
//        if (LazDataPools.getInstance().isHomeVersionV7()
//                && !LazDataPools.getInstance().isHideJfyTab()) {
//            viewPager.setBackground(new RecommendContainerDrawable(context));
//        } else {
//            viewPager.setBackgroundColor(Color.parseColor(GREY_BG_F0F1F6));
//        }
        viewPager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, targetHeight - tabHeight));

        final ViewPagerAdapter adapter = new ViewPagerAdapter(context, viewPager);
        adapter.setTabItems(Arrays.asList(strArray));
//        adapter.setRecommendRepo(recommendRepo);

        if (parent instanceof UpdatedNestedRecyclerView) {
            adapter.setParentRecyclerView((UpdatedNestedRecyclerView) parent);
        }
        viewPager.setAdapter(adapter);

        sViewPagerRef = new WeakReference<>(viewPager);
        slidingTabLayoutRevamp.setViewPager(viewPager);

        // get tab resource firstly
//        final IRecommendTabResource tabResource = recommendRepo.getRecommendTabs();
//        List<JSONObject> list = tabResource.getTabItems();
//        if (list != null) {
//            LLog.i(TAG, "create container after get tab resource");
////            List<JSONObject> oldList = new ArrayList<>();
////            oldList.addAll(adapter.getTabItems());
//            List<JSONObject> oldList = new ArrayList<>(adapter.getTabItems());
//            if (isDifferent(oldList, list)) {
//                adapter.setTabItems(list);
//                slidingTabLayoutRevamp.setIndicatorColor(tabResource.getIndicatorColor());
//                slidingTabLayoutRevamp.setFixTabFlag(tabResource.isFixedTab());
//                slidingTabLayoutRevamp.setViewPager(viewPager);
//
//                if(!oldList.isEmpty()) {
//                    adapter.resetState();
//                } else {
//                    viewPager.setCurrentItem(adapter.defaultPage);
//                }
//            } else {
//                slidingTabLayoutRevamp.setIndicatorColor(tabResource.getIndicatorColor());
//                slidingTabLayoutRevamp.setFixTabFlag(tabResource.isFixedTab());
//                slidingTabLayoutRevamp.updateTabStyles(list);
//            }
//
//            // update the relationship of tabs
//            adapter.updateTabInfos(list);
//        }

        // tab data refresh listener
//        if (BaseUtils.isMemoryOpt(context) ) {
//            Log.e(TAG, "createRecommendContainer: opt!!!!");
//            tabResource.setTabResourceListener(new TabResourceListenerImpl(adapter, slidingTabLayoutRevamp, viewPager));
//        } else {
//            Log.e(TAG, "createRecommendContainer: not opt!!!!");
//            tabResource.setTabResourceListener(new IRecommendTabResource.TabResourceListener() {
//                @Override
//                public void notifyTabResource(List<JSONObject> list) {
//                    //LLog.i(TAG, "response ---> tab resource success list is: " + list);
//
////                List<JSONObject> oldList = new ArrayList<>();
////                oldList.addAll(adapter.getTabItems());
//                    List<JSONObject> oldList = new ArrayList<>(adapter.getTabItems());
//                    if ( isDifferent(oldList, list) ) {
//                        adapter.setTabItems(list);
//                        slidingTabLayoutRevamp.setIndicatorColor(tabResource.getIndicatorColor());
//                        slidingTabLayoutRevamp.setFixTabFlag(tabResource.isFixedTab());
//                        slidingTabLayoutRevamp.setViewPager(viewPager);
//                        if ( !oldList.isEmpty() ) {
//                            adapter.resetState();
//                        } else {
//                            viewPager.setCurrentItem(adapter.defaultPage);
//                        }
//                    } else {
//                        slidingTabLayoutRevamp.setIndicatorColor(tabResource.getIndicatorColor());
//                        slidingTabLayoutRevamp.setFixTabFlag(tabResource.isFixedTab());
//                        slidingTabLayoutRevamp.updateTabStyles(list);
//                    }
//
//                    if ( rmdCacheListener != null ) {
//                        rmdCacheListener.getTabData();
//                    }
//
//                    // update the relationship of tabs
//                    adapter.updateTabInfos(list);
//                }
//            });
//        }

        layout.addView(viewPager);
        return layout;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int pos) {
        if(viewHolder instanceof UpdatedTextViewHolder) {
            Log.d("gaohui","pos " + pos + mDataList.get(pos) + mDataList.get(pos));
            ( (UpdatedTextViewHolder)viewHolder).mTv.setText((String)mDataList.get(pos));
            final  String text = (String) mDataList.get(pos);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(viewHolder.itemView.getContext(),text + "Clicked",Toast.LENGTH_SHORT).show();
                }
            });
        } else if(viewHolder instanceof UpdatedCategoryViewHolder) {
            ((UpdatedCategoryViewHolder)viewHolder).bindData(mDataList.get(pos));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
