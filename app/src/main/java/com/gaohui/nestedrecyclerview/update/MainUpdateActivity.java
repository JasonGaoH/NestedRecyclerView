package com.gaohui.nestedrecyclerview.update;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.widget.Toast;

import com.gaohui.nestedrecyclerview.BaseMenuActivity;
import com.gaohui.nestedrecyclerview.R;
//import com.gaohui.nestedrecyclerview.java.adapter.MultiTypeAdapter;
import com.gaohui.nestedrecyclerview.java.bean.CategoryBean;
import com.gaohui.nestedrecyclerview.update.NestedStaggeredGridLayoutManager;
import com.gaohui.nestedrecyclerview.update.UpdatedNestedRecyclerView;
import com.gaohui.nestedrecyclerview.update.adpter.UpdatedMultiTypeAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class MainUpdateActivity extends BaseMenuActivity {

    ArrayList<Object> mDataList = new ArrayList<Object>();

//    MultiTypeAdapter adapter = new MultiTypeAdapter(mDataList);

    UpdatedMultiTypeAdapter adapter = new UpdatedMultiTypeAdapter(mDataList);

//    StoreSwipeRefreshLayout storeSwipeRefreshLayout;
    UpdatedNestedRecyclerView updatedNestedRecyclerView;

    Long lastBackPressedTime = 0L;

    String[] strArray = new String[]{"推荐", "视频", "直播", "图片", "精华", "热门"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_update);

        updatedNestedRecyclerView = findViewById(R.id.recyclerView);

//        storeSwipeRefreshLayout = findViewById(R.id.refresh_layout);
        updatedNestedRecyclerView.setAdapter(adapter);

        NestedStaggeredGridLayoutManager mLayoutManager = new NestedStaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL, updatedNestedRecyclerView);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        updatedNestedRecyclerView.setLayoutManager(mLayoutManager);

        refresh();

//        storeSwipeRefreshLayout.setColorSchemeColors(Color.RED);
//        storeSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                refresh();
//            }
//        });

    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - lastBackPressedTime < 2000) {
            super.onBackPressed();
        } else  {
            updatedNestedRecyclerView.scrollToPosition(0);
            Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
            lastBackPressedTime = System.currentTimeMillis();
        }
    }

    private void refresh() {
        mDataList.clear();
        for(int i = 0;i<8;i++) {
            mDataList.add("parent item text " + i );
        }
        CategoryBean categoryBean = new CategoryBean();
        categoryBean.getTabTitleList().clear();
        categoryBean.getTabTitleList().addAll(Arrays.asList(strArray));
        mDataList.add(categoryBean);
        adapter.notifyDataSetChanged();
//        storeSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        adapter.destroy();
    }
}