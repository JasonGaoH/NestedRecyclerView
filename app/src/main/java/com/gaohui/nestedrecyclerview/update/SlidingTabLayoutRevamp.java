package com.gaohui.nestedrecyclerview.update;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.gaohui.nestedrecyclerview.R;
import com.gaohui.nestedrecyclerview.update.viewpager.ViewPagerAdapter;
import com.gaohui.nestedrecyclerview.update.utils.ScreenUtils;

import java.util.List;

/**
 * @author: zhuhuihui
 * @date: 2019/6/13
 * @description: tabs for just for you
 * /**
 * sliding tab layout and have strong relationshipi with view pager
 */
public class SlidingTabLayoutRevamp extends HorizontalScrollView implements ViewPager.OnPageChangeListener, IRecommendTabLayout {
    public static final String TAG = "SlidingTabLayoutRevamp";

    public static final String TAB_ID = "tabId";


    private static final String UNSELECT_TITLE_COLOR = "#595F6D";
    private final Context mContext;
    private ViewPager mViewPager;
    private final LinearLayout mTabsContainer;
    private int mCurrentTab;
    private float mCurrentPositionOffset;
    private int mTabCount;
    /**
     * draw indicator
     */
    private final Rect mIndicatorRect = new Rect();
    /**
     * scroll and position center
     */
    private final Rect mTabRect = new Rect();
    private final GradientDrawable mIndicatorDrawable = new GradientDrawable();

    /**
     * the bottom separator line
     */
    private boolean lineDrawable = false;
    private final GradientDrawable mLineDrawable = new GradientDrawable();

    /**
     * indicator
     */
    private int mIndicatorColor;
    private float mIndicatorHeight;
    private int mIndicatorPadding;
    private float mIndicatorCornerRadius;
    private int mLastScrollX;
    private final int mUnSelectTitleColor = Color.parseColor(UNSELECT_TITLE_COLOR);

    /**
     * tabs data
     * JSONObject must contains TAB_ID
     */
    private List<String> tabItems;

    private String selectedTabId = "";

    /**
     * true means fixed
     */
    private boolean mIsFixedTab;

    /**
     * 标记JFY TAB是否吸顶
     */
    private boolean mJfyAtTop;

    public SlidingTabLayoutRevamp(Context context) {
        this(context, null, 0);
    }

    public SlidingTabLayoutRevamp(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayoutRevamp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /**
         * true 设置滚动视图是否可以伸缩其内容以填充视口
         */
        //setFillViewport(true);
        setFillViewport(false);
        /**
         * 重写onDraw方法,需要调用这个方法来清除flag
         */
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);

        this.mContext = context;
        int adaptSize = 50;
        setPadding(adaptSize, 0, adaptSize, 0);
        mTabsContainer = new LinearLayout(context);
        addView(mTabsContainer);
        obtainAttributes(context, attrs);
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
//        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayoutRevamp);
//
//        mIndicatorColor = ta.getColor(R.styleable.SlidingTabLayoutRevamp_tl_indicator_color_revamp, Color.parseColor("#ff330c"));
//        mIndicatorHeight = ta.getDimension(R.styleable.SlidingTabLayoutRevamp_tl_indicator_height_revamp,
//                dp2px(3));
//        mIndicatorWidth = ta.getDimension(R.styleable.SlidingTabLayoutRevamp_tl_indicator_width_revamp, dp2px(-1));
//        mIndicatorCornerRadius = ta.getDimension(R.styleable.SlidingTabLayoutRevamp_tl_indicator_corner_radius_revamp, dp2px(2));
//        mIndicatorMarginLeft = ta.getDimension(R.styleable.SlidingTabLayoutRevamp_tl_indicator_margin_left_revamp, dp2px(0));
//        mIndicatorMarginRight = ta.getDimension(R.styleable.SlidingTabLayoutRevamp_tl_indicator_margin_right_revamp, dp2px(0));
//        mIndicatorMarginBottom = ta.getDimension(R.styleable.SlidingTabLayoutRevamp_tl_indicator_margin_bottom_revamp, dp2px(0));
//
//        ta.recycle();

        mIndicatorColor = Color.RED;
        mIndicatorHeight = dp2px(3);
        mIndicatorPadding = dp2px(3);
        mIndicatorCornerRadius = dp2px(12);
    }

//    private OnLayoutListener mOnLayoutListener;
//
//    @Override
//    public void setOnLayoutListener(OnLayoutListener onLayoutListener) {
//        mOnLayoutListener = onLayoutListener;
//    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        try {
            if (getChildCount() > 0) {
                int maxScroll = getChildAt(0).getWidth()
                        - (getWidth() - getPaddingLeft() - getPaddingRight());
//                if (mOnLayoutListener != null) {
//                    mOnLayoutListener.onScrollChanged(getScrollX());
//                    if (getScrollX() == 0) {
//                        mOnLayoutListener.onReachStart();
//                    } else if (getScrollX() == maxScroll) {
//                        mOnLayoutListener.onReachEnd();
//                    }
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView() {
        return this;
    }

    /**
     * related ViewPager
     */
    @Override
    public void setViewPager(ViewPager vp) {
        if (vp == null || vp.getAdapter() == null) {
            throw new IllegalStateException("ViewPager or ViewPager adapter can not be NULL !");
        }

        this.mViewPager = vp;

        this.mViewPager.removeOnPageChangeListener(this);
        this.mViewPager.addOnPageChangeListener(this);
        if (mViewPager.getAdapter() instanceof ViewPagerAdapter) {
            tabItems = ((ViewPagerAdapter) mViewPager.getAdapter()).tabItems;
        }
        if (tabItems != null && tabItems.size() > 0 && mViewPager.getCurrentItem() < tabItems.size()) {
//            selectedTabId = tabItems.get(mViewPager.getCurrentItem()).getString(TAB_ID);
//            LazDataPools.getInstance().setSelectedJFYTabId(selectedTabId);
        }
        notifyDataSetChanged();
    }

    /**
     * true means fixed
     *
     * @param fixTabFlag
     */
    @Override
    public void setFixTabFlag(boolean fixTabFlag) {
        mIsFixedTab = fixTabFlag;
    }

    /**
     * 更新数据
     * 因为tab数量有可能增减而且tab状态有收起与展开两种形态，所以此处更新时要注意状态
     * 不同状态的差距只有副标题是否隐藏、分割线是否展示及topMargin
     */
    public void notifyDataSetChanged() {
        addAllTabs();
        updateTabStyles();

        // update the bg of sliding tab bar
        updateSlidingTabBackground();
    }

    private void updateSlidingTabBackground() {
        if (tabItems == null || tabItems.isEmpty()) {
            return;
        }

        if (getParent() == null) {
            return;
        }


        if (getParent() instanceof FrameLayout) {
//            ((FrameLayout) getParent()).setBackgroundColor(Color.parseColor(GREY_BG_F0F1F6));
        }
    }

    private boolean isFixedTabModel() {
        return mIsFixedTab;
    }

    private void addAllTabs() {
        // 如果mTabsContainer含有子view，则是非首次更新，记录当前状态
        mTabsContainer.removeAllViews();
        this.mTabCount = tabItems == null ? 0 : tabItems.size();
        View tabView;
        for (int i = 0; i < mTabCount; i++) {
            try {
                tabView = LayoutInflater.from(mContext).inflate(R.layout.laz_recommend_tab_filter_revamp_new_rec, mTabsContainer, false);
//                HpImageUtils.attachHomePageTag(tabView);
                addTab(i, tabView);
            } catch (Throwable t) {
                // pass
            }
        }
    }

    /**
     * add and create tab item
     */
    private void addTab(final int position, View tabView) {
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mTabsContainer.indexOfChild(v);
                if (position != -1) {
                    if (mViewPager.getCurrentItem() != position) {
                        if (position < tabItems.size()) {
                            mViewPager.setCurrentItem(position, false);
                        }
                    }
                }
            }
        });
        mTabsContainer.addView(tabView, position);

        // add touch feedback
//        TouchFeedbackUtils.addTouchFeedback(tabView, true, true);
    }

    private void initTabView(final View tabView, String tabItem, boolean isSelected) {
        if (tabView == null || tabItem == null) {
            return;
        }

        // Step2: 设置tab顶部icon
//        String festival = isSelected ? tabItem.getString(TAB_SELECTED_TOP_IMG) : tabItem.getString(TAB_UNSELECTED_TOP_IMG);
//
//        if (isSelected && TextUtils.isEmpty(festival)) {
//            festival = tabItem.getString(TAB_UNSELECTED_TOP_IMG);
//        }
//
//        // Step3: 设置icon
//        TUrlImageView festivalView = tabView.findViewById(R.id.tab_icon);
//        // add placeholder
//        festivalView.setPlaceHoldImageResId(R.drawable.hp_banner_h100_placeholder);
//        festivalView.setErrorImageResId(R.drawable.hp_banner_h100_placeholder);
//        HpImageUtils.dealWithGifImage(festival, festivalView);
//        HpImageUtils.adapterBitmapToWidget(festivalView, festival, LazHPDimenUtils.adaptTwentyOneDpToPx(mContext), 0);
//
//        // Step4: 设置title
        TextView titleView = tabView.findViewById(R.id.tab_title);
        titleView.setText(tabItem);
//        if (I18NMgt.getInstance(LazGlobal.sApplication).getENVCountry() == Country.TH
//                && I18NMgt.getInstance(LazGlobal.sApplication).getENVLanguage() == Language.TH_TH) {
//            //泰文减小行间距，否则会被裁剪
//            titleView.setLineSpacing(0, 0.8f);
//        } else {
//            titleView.setLineSpacing(0, 1.0f);
//        }

        refreshTabTitle(tabView, tabItem, isSelected);
    }

    @Override
    public View getTabAt(int position) {
        if (mTabsContainer != null && position < mTabsContainer.getChildCount()) {
            return mTabsContainer.getChildAt(position);
        }
        return null;
    }

    @Override
    public void setLineDrawableEnabled(boolean enabled) {
        lineDrawable = enabled;
        mLineDrawable.setColor(Color.parseColor("#eeeeee"));
        mLineDrawable.setBounds(0, getHeight() - dp2px(0.5f), mTabsContainer.getWidth(), getHeight());
        invalidate();
    }

    @Override
    public void setJfyAtTop(boolean jfyAtTop) {
        mJfyAtTop = jfyAtTop;
        invalidate();
    }

    public void updateTabStyles() {
        for (int i = 0; i < mTabCount; i++) {
            View v = mTabsContainer.getChildAt(i);
            initTabView(v, tabItems.get(i), selectedTabId.equals(tabItems.get(i)));
        }

        invalidate();
    }

    @Override
    public void updateTabStyles(List<String> list) {
        this.tabItems.clear();
        this.tabItems.addAll(list);
        mTabCount = tabItems.size();
        updateSlidingTabBackground();
        for (int i = 0; i < mTabCount; i++) {
            View v = mTabsContainer.getChildAt(i);
            initTabView(v, tabItems.get(i), selectedTabId.equals(tabItems.get(i)));
        }
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /**
         * position:当前View的位置
         * mCurrentPositionOffset:当前View的偏移量比例.[0,1)
         */
        this.mCurrentTab = position;
        this.mCurrentPositionOffset = positionOffset;
        scrollToCurrentTab();
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        if (position < tabItems.size()) {
            updateTabSelection(position);
            // 对于Lazada，点击和滚动到某个tab，都需要一个点击事件；对于滚动，只需要统计滚动行为（当前是在ViewPagerAdapter中）
//            final String spm = HpSPMUtil.buildHomeSPM(SPMConstants.HOME_22_JFY_TABS, position);
//            String tabType = tabItems.get(position).getString(TAB_NAME_KEY);
//            final Map<String, String> args = new HashMap<>();
//            if (!TextUtils.isEmpty(tabType)) {
//                args.put("tabType", tabType);
//            }
//            HpSPMUtil.trackClickEventV2(SPMConstants.HOME_PAGE, SPMConstants.UT_TRACK_JFY_TAB_CLICK,
//                    spm,
//                    args);
//
//            // TODO: current is all tab
//            if (position == 0) {
//                LazDataPools.getInstance().setJfyLastTimeMs(System.currentTimeMillis());
//            }
//
//            // send event to notify recommend tab change
//            EventCenter.getInstance().post(new RecommendTabChangeEvent(selectedTabId));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * HorizontalScrollView scroll to current tab and at the center of whole sliding tab layout
     */
    private void scrollToCurrentTab() {
        if (mTabCount <= 0) {
            return;
        }

        if (mTabsContainer == null || mTabsContainer.getChildAt(mCurrentTab) == null) {
            return;
        }

        int offset = (int) (mCurrentPositionOffset * mTabsContainer.getChildAt(mCurrentTab).getWidth());
        /**当前Tab的left+当前Tab的Width乘以positionOffset*/
        int newScrollX = mTabsContainer.getChildAt(mCurrentTab).getLeft() + offset;

        if (mCurrentTab > 0 || offset > 0) {
            /**HorizontalScrollView移动到当前tab,并居中*/
            newScrollX -= getWidth() / 2 - getPaddingLeft();
            calcIndicatorRect();
            newScrollX += ((mTabRect.right - mTabRect.left) / 2);
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            /** scrollTo（int x,int y）:x,y代表的不是坐标点,而是偏移量
             *  x:表示离起始位置的x水平方向的偏移量
             *  y:表示离起始位置的y垂直方向的偏移量
             */
            smoothScrollTo(newScrollX, 0);
        }
    }

    private void updateTabSelection(int position) {
        if (position >= tabItems.size() || position < 0) {
            return;
        }

//        selectedTabId = tabItems.get(position).getString(TAB_ID);
//        LazDataPools.getInstance().setSelectedJFYTabId(selectedTabId);

        for (int i = 0; i < mTabCount; ++i) {
            final View tabView = mTabsContainer.getChildAt(i);
            final boolean isSelect = i == position;
            if (tabItems.size() <= i) {
                // 并发异常了
                break;
            }
            String item = tabItems.get(i);
            refreshTabTitle(tabView, item, isSelect);
        }
    }

    private void calcIndicatorRect() {
        if (mCurrentTab < 0 || mCurrentTab >= mTabsContainer.getChildCount()) {
            return;
        }

        View currentTabView = mTabsContainer.getChildAt(this.mCurrentTab);
        if (currentTabView == null) {
            return;
        }

        float left = currentTabView.getLeft();
        float right = currentTabView.getRight();

        if (this.mCurrentTab < mTabCount - 1) {
            View nextTabView = mTabsContainer.getChildAt(this.mCurrentTab + 1);
            float nextTabLeft = nextTabView.getLeft();
            float nextTabRight = nextTabView.getRight();

            left = left + mCurrentPositionOffset * (nextTabLeft - left);
            right = right + mCurrentPositionOffset * (nextTabRight - right);
        }

        mIndicatorRect.left = (int) (left);
        mIndicatorRect.right = (int) (right);

        mTabRect.left = (int) left;
        mTabRect.right = (int) right;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || mTabCount <= 0) {
            return;
        }

        int height = getHeight();
        int paddingLeft = getPaddingLeft();

        if (lineDrawable) {
            mLineDrawable.draw(canvas);
        }

        //计算indicator位置
        calcIndicatorRect();

        if (mIndicatorHeight > 0) {
            if (mJfyAtTop) {
                mIndicatorDrawable.setColor(mIndicatorColor);
                mIndicatorDrawable.setBounds(paddingLeft + mIndicatorRect.left + mIndicatorPadding,
                        height - (int) mIndicatorHeight,
                        paddingLeft + mIndicatorRect.right - mIndicatorPadding,
                        height);
            } else {
                mIndicatorDrawable.setColor(Color.WHITE);

                mIndicatorDrawable.setBounds(paddingLeft + mIndicatorRect.left,
                        0, paddingLeft + mIndicatorRect.right, height);

                mIndicatorDrawable.setStroke(dp2px(1.0f), mIndicatorColor);
            }
            mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
            mIndicatorDrawable.draw(canvas);
        }
    }

    @Override
    public void setIndicatorColor(int indicatorColor) {
        if (indicatorColor != 0) {
            this.mIndicatorColor = indicatorColor;
            invalidate();
        }
    }

    public void setIndicatorHeight(float indicatorHeight) {
        this.mIndicatorHeight = dp2px(indicatorHeight);
        invalidate();
    }

    public void setIndicatorCornerRadius(float indicatorCornerRadius) {
        this.mIndicatorCornerRadius = dp2px(indicatorCornerRadius);
        invalidate();
    }

    @Override
    public int getTabCount() {
        return mTabCount;
    }

    @Override
    public void setOnLayoutListener(OnLayoutListener onLayoutListener) {

    }

    private int dp2px(float dp) {
        return ScreenUtils.dp2px(getContext(), dp);
    }

    private void refreshTabTitle(View view, String tabItem, boolean isSelected) {
        // refresh top image
//        String festival = isSelected ? tabItem.getString(TAB_SELECTED_TOP_IMG) : tabItem.getString(TAB_UNSELECTED_TOP_IMG);
//
//        if (isSelected && TextUtils.isEmpty(festival)) {
//            festival = tabItem.getString(TAB_UNSELECTED_TOP_IMG);
//        }
//        HpImageUtils.dealWithGifImage(festival, ((TUrlImageView) view.findViewById(R.id.tab_icon)));
//        HpImageUtils.adapterBitmapToWidget(((TUrlImageView) view.findViewById(R.id.tab_icon)), festival, LazHPDimenUtils.adaptTwentyOneDpToPx(mContext), 0);

        // refresh text
//        int titleSelectedColor = (mIndicatorColor == 0) ?
//                ContextCompat.getColor(getContext(), R.color.laz_common_FE4960) : mIndicatorColor;
//        int titleColor = isSelected ? titleSelectedColor : mUnSelectTitleColor;
//        TextView titleTV = (TextView) view.findViewById(R.id.tab_title);
//        titleTV.setTextColor(titleColor);
//        if (isSelected) {
//            titleTV.setTypeface(FontStyle.getCurrentTypeface(getContext(), FontStyle.STYLE_BOLD));
//        } else {
//            titleTV.setTypeface(FontStyle.getCurrentTypeface(getContext(), FontStyle.STYLE_SEMI_BOLD));
//        }
    }
}
