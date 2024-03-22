package com.gaohui.nestedrecyclerview.update.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;


public class UpdatedCategoryViewHolder extends RecyclerView.ViewHolder {

//    private TabLayout mTabLayout;
//    private ViewPager mViewPager;

//    private ChildRecyclerView mCurrentRecyclerView;
//
//    HashMap<String, CategoryView> cacheVies = new  HashMap<String, CategoryView>();
//
//
//    ArrayList<CategoryView> viewList = new ArrayList<CategoryView>();


    public UpdatedCategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        Log.d("gaohui","11111 category" + itemView.getLayoutParams());
        StaggeredGridLayoutManager.LayoutParams slp;
        if ((itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams)) {
            slp = (StaggeredGridLayoutManager.LayoutParams)itemView.getLayoutParams();
            slp.setFullSpan(true);
            Log.d("gaohui","22222 category");
        }
    }

    public void bindData(Object obj) {
//        if(obj instanceof CategoryBean) {
//            CategoryBean categoryBean = (CategoryBean)obj;
//            viewList.clear();
//            if(cacheVies.size() > categoryBean.getTabTitleList().size()) {
//                cacheVies.clear();
//            }
//            for(String str :categoryBean.getTabTitleList()) {
//                CategoryView categoryView = cacheVies.get(str);
//                if(categoryView == null || categoryView.getParent() != mViewPager) {
//                    categoryView = new CategoryView(mViewPager.getContext());
//                    cacheVies.put(str, categoryView);
//                }
//                viewList.add(categoryView);
//            }
//            mCurrentRecyclerView = viewList.get(mViewPager.getCurrentItem());
//            int lastItem = mViewPager.getCurrentItem();
//            mViewPager.setAdapter(new CategoryPagerAdapter(viewList,categoryBean.getTabTitleList()));
//            mTabLayout.setupWithViewPager(mViewPager);
//            mViewPager.setCurrentItem(lastItem);
//
//        }
    }

    public void destroy() {
//        cacheVies.clear();
    }

//    public ChildRecyclerView getCurrentChildRecyclerView() {
//        return mCurrentRecyclerView;
//    }
}
