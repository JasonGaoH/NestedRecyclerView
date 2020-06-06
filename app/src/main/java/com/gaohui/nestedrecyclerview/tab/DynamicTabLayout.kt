package com.gaohui.nestedrecyclerview.tab

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.graphics.*
import android.support.annotation.ColorInt
import android.support.annotation.IntDef
import android.support.annotation.RestrictTo
import android.support.v4.util.Pools
import android.support.v4.view.*
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.Gravity
import android.view.SoundEffectConstants
import android.view.View
import android.view.View.MeasureSpec.*
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.gaohui.nestedrecyclerview.R
import java.lang.ref.WeakReference
import java.util.*

/**
 * 可以折叠的TabLayout
 *
 * @constructor
 * @param context
 * @param attrs
 * @param defStyleAttr
 */
@ViewPager.DecorView
class DynamicTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    HorizontalScrollView(context, attrs, defStyleAttr) {
    /**
     * Mode
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(value = [MODE_SCROLLABLE, MODE_FIXED])
    @Retention(AnnotationRetention.SOURCE)
    annotation class Mode

    /**
     * TabGravity
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(flag = true, value = [GRAVITY_FILL, GRAVITY_CENTER])
    @Retention(AnnotationRetention.SOURCE)
    annotation class TabGravity


    /**
     * Callback interface invoked when a tab's selection state changes.
     */
    interface OnTabSelectedListener {
        /**
         * Called when a tab enters the selected state.
         *
         * @param tab The tab that was selected
         */
        fun onTabSelected(tab: Tab)

        /**
         * Called when a tab exits the selected state.
         *
         * @param tab The tab that was unselected
         */
        fun onTabUnselected(tab: Tab?)

        /**
         * Called when a tab that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category.
         *
         * @param tab The tab that was reselected.
         */
        fun onTabReselected(tab: Tab?)
    }

    /**
     * DynamicTabProvider
     */
    interface DynamicTabProvider {
        /**
         * getPageTitleItem
         *
         * @param position
         * @return
         */
        fun getPageTitleItem(position: Int): DynamicTabBean?
    }

    private val mTabs = ArrayList<Tab>()
    private var mSelectedTab: Tab? = null
    private val mTabStrip: SlidingTabStrip
    var mTabPaddingStart: Int
    var mTabPaddingTop: Int
    var mTabPaddingEnd: Int
    var mTabPaddingBottom: Int
    private val mTabBackgroundResId: Int
    var tabMaxWidth = Int.MAX_VALUE
    private val mRequestedTabMinWidth: Int
    private val mRequestedTabMaxWidth: Int
    private val mScrollableTabMinWidth: Int
    private val mContentInsetStart: Int
    var mTabGravity: Int
    var mMode: Int

    private val mSelectedListeners = ArrayList<OnTabSelectedListener>()
    private var mCurrentVpSelectedListener: OnTabSelectedListener? = null
    private var mScrollAnimator: ValueAnimator? = null
    var mViewPager: ViewPager? = null
    private var mPagerAdapter: PagerAdapter? = null
    private var mPagerAdapterObserver: DataSetObserver? = null
    private var mPageChangeListener: TabLayoutOnPageChangeListener? = null
    private var mAdapterChangeListener: AdapterChangeListener? = null
    private var mSetupViewPagerImplicitly = false
    // Pool we use as a simple RecyclerBin
    private val mTabViewPool: Pools.Pool<TabView>? = Pools.SimplePool(12)

    var isTabDescFold = false
    private var animationProgress = 1f
    private var mObjectAnimator:ObjectAnimator? = null

    init {
        // Disable the Scroll Bar
        isHorizontalScrollBarEnabled = false
        mTabStrip = SlidingTabStrip(context)
        super.addView(mTabStrip, 0, LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        val a = context.obtainStyledAttributes(attrs, R.styleable.DynamicTabLayout,
            defStyleAttr, 0)
        val selectedIndicatorHeight = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabIndicatorHeight, 0)
        mTabStrip.setSelectedIndicatorHeight(selectedIndicatorHeight)
        val selectedIndicatorWidth = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabIndicatorWidth, dpToPx(28))
        mTabStrip.setSelectedIndicatorWidth(selectedIndicatorWidth)
        mTabStrip.setSelectedIndicatorColor(a.getColor(R.styleable.DynamicTabLayout_dtl_tabIndicatorColor, 0))
        mTabPaddingBottom = a
            .getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabPadding, 0)
        mTabPaddingEnd = mTabPaddingBottom
        mTabPaddingTop = mTabPaddingEnd
        mTabPaddingStart = mTabPaddingTop
        mTabPaddingStart = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabPaddingStart,
            mTabPaddingStart)
        mTabPaddingTop = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabPaddingTop,
            mTabPaddingTop)
        mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabPaddingEnd,
            mTabPaddingEnd)
        mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabPaddingBottom,
            mTabPaddingBottom)
        mTabStrip.setSelectedIndicatorOffset(
            a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabIndicatorOffset, dpToPx(0)))
        mTabStrip.setSupportTabIndicatorAnimation(
            a.getBoolean(R.styleable.DynamicTabLayout_dtl_tabIndicatorAnimation, false))
        mTabStrip.setIndicatorRoundRadiusX(a.getFloat(R.styleable.DynamicTabLayout_dtl_tabIndicatorRadiusX, selectedIndicatorWidth.toFloat()))
        mTabStrip.setIndicatorRoundRadiusY(a.getFloat(R.styleable.DynamicTabLayout_dtl_tabIndicatorRadiusY, selectedIndicatorWidth.toFloat()))
        mRequestedTabMinWidth = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabMinWidth,
            INVALID_WIDTH)
        mRequestedTabMaxWidth = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabMaxWidth,
            INVALID_WIDTH)
        mTabBackgroundResId = a.getResourceId(R.styleable.DynamicTabLayout_dtl_tabBackground, 0)
        mContentInsetStart = a.getDimensionPixelSize(R.styleable.DynamicTabLayout_dtl_tabContentStart, 0)
        mMode = a.getInt(R.styleable.DynamicTabLayout_dtl_tabMode, MODE_SCROLLABLE)
        mTabGravity = a.getInt(R.styleable.DynamicTabLayout_dtl_tabGravity, GRAVITY_FILL)
        a.recycle()
        val res = resources
        mScrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.design_tab_scrollable_min_width)
        applyModeAndGravity()
    }

    /**
     * Sets the tab indicator's color for the currently selected tab.
     *
     * @param color color to use for the indicator
     * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorColor
     */
    fun setSelectedTabIndicatorColor(@ColorInt color: Int) {
        mTabStrip.setSelectedIndicatorColor(color)
    }

    /**
     * setIndicatorOffset
     *
     * @param offset
     */
    fun setIndicatorOffset(offset: Int) {
        mTabStrip.setSelectedIndicatorOffset(offset)
    }

    /**
     * Sets the tab indicator's height for the currently selected tab.
     *
     * @param height height to use for the indicator in pixels
     * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorHeight
     */
    fun setSelectedTabIndicatorHeight(height: Int) {
        mTabStrip.setSelectedIndicatorHeight(height)
    }

    /**
     * 设置Indicator的宽度
     *
     * @param width
     */
    fun setSelectedTabIndicatorWidth(width: Int) {
        mTabStrip.setSelectedIndicatorWidth(width)
    }

    /**
     * 设置是否支持Indicator的粘滞动画
     *
     * @param support
     */
    fun setSupportTabIndicator(support: Boolean) {
        mTabStrip.setSupportTabIndicatorAnimation(support)
    }

    /**
     * 改变子标题的高度（支持动画）
     *
     * @param fold
     */
    fun changeDescHeightWithAnimation(fold:Boolean) {
        if(fold != isTabDescFold) {
            isTabDescFold = fold
            val endTarget = if(fold) {
                0f
            } else {
                1f
            }
            if(mObjectAnimator != null && mObjectAnimator?.isRunning == true) {
                mObjectAnimator?.cancel()
            }
            mObjectAnimator = ObjectAnimator.ofFloat(this,"animationProgress",
                getAnimationProgress(),endTarget).apply {
                duration = 300
                interpolator = LinearInterpolator()
                start()
            }
        }
    }

    /**
     * 属性动画 getAnimationProgress
     *
     * @return
     */
    fun getAnimationProgress():Float {
        return animationProgress
    }

    /**
     * 属性动画 setAnimationProgress
     *
     * @param progress
     */
    fun setAnimationProgress(progress:Float) {
        animationProgress = progress
        for(i in 0 until mTabStrip.childCount) {
            val child  = mTabStrip.getChildAt(i)
            if(child is TabView) {
                child.changeProgress(progress)
            }
        }
        mTabStrip
    }

    /**
     * Set the scroll position of the tabs. This is useful for when the tabs are being displayed as
     * part of a scrolling container such as [ViewPager].
     *
     *
     * Calling this method does not update the selected tab, it is only used for drawing purposes.
     *
     * @param position           current scroll position
     * @param positionOffset     Value from [0, 1) indicating the offset from `position`.
     * @param updateSelectedText Whether to update the text's selected state.
     */
    private fun setScrollPosition(position: Int, positionOffset: Float, updateSelectedText: Boolean) {
        setScrollPosition(position, positionOffset, updateSelectedText, true)
    }

    /**
     * 设置当前需要滑动到那个position
     *
     * @param position
     * @param positionOffset
     * @param updateSelectedText
     * @param updateIndicatorPosition
     */
    fun setScrollPosition(position: Int, positionOffset: Float, updateSelectedText: Boolean,
                          updateIndicatorPosition: Boolean) {
        val roundedPosition = Math.round(position + positionOffset)
        if (roundedPosition < 0 || roundedPosition >= mTabStrip.childCount) {
            return
        }
        // Set the indicator position, if enabled
        if (updateIndicatorPosition) {
            mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset)
        }
        // Now update the scroll position, canceling any running animation
        if (mScrollAnimator != null && mScrollAnimator?.isRunning == true) {
            mScrollAnimator?.cancel()
        }
        scrollTo(calculateScrollXForTab(position, positionOffset), 0)
        // Update the 'selected state' view as we scroll, if enabled
        if (updateSelectedText) {
            setSelectedTabView(roundedPosition)
        }
    }

    private val scrollPosition: Float
        get() = mTabStrip.indicatorPosition
    /**
     * Add a tab to this layout. The tab will be added at the end of the list.
     *
     * @param tab         Tab to add
     * @param setSelected True if the added tab should become the selected tab.
     */
    /**
     * Add a tab to this layout. The tab will be added at the end of the list.
     * If this is the first tab to be added it will become the selected tab.
     *
     * @param tab Tab to add
     */
    @JvmOverloads
    fun addTab(tab: Tab, setSelected: Boolean = mTabs.isEmpty()) {
        addTab(tab, mTabs.size, setSelected)
    }

    /**
     * Add a tab to this layout. The tab will be inserted at `position`.
     *
     * @param tab
     * @param position
     * @param setSelected
     */
    @JvmOverloads
    fun addTab(tab: Tab, position: Int, setSelected: Boolean = mTabs.isEmpty()) {
        require(!(tab.mParent !== this)) { "Tab belongs to a different TabLayout." }
        configureTab(tab, position)
        addTabView(tab)
        if (setSelected) {
            tab.select()
        }
    }

    private fun addTabFromItemView() {
        val tab = newTab()
        addTab(tab)
    }

    /**
     * Add a [OnTabSelectedListener] that will be invoked when tab selection
     * changes.
     *
     * @param listener
     */
    fun addOnTabSelectedListener(listener: OnTabSelectedListener) {
        if (!mSelectedListeners.contains(listener)) {
            mSelectedListeners.add(listener)
        }
    }

    /**
     *  Remove the given [OnTabSelectedListener]
     *
     * @param listener
     */
    fun removeOnTabSelectedListener(listener: OnTabSelectedListener) {
        mSelectedListeners.remove(listener)
    }

    /**
     * Remove all previously added [OnTabSelectedListener].
     */
    fun clearOnTabSelectedListeners() {
        mSelectedListeners.clear()
    }

    /**
     *  Create and return a new [Tab]
     *
     * @return
     */
    fun newTab(): Tab {
        val tab = Tab()
        tab.mParent = this
        tab.mView = createTabView(tab)
        return tab
    }

    /**
     * tabCount
     */
    val tabCount: Int
        get() = mTabs.size

    /**
     * Returns the tab at the specified index.
     */
    fun getTabAt(index: Int): Tab? {
        return if (index < 0 || index >= tabCount) null else mTabs[index]
    }

    /**
     * Returns the position of the current selected tab.
     *
     * @return selected tab position, or `-1` if there isn't a selected tab.
     */
    val selectedTabPosition: Int
        get() = mSelectedTab?.position ?: -1

    /**
     * Remove a tab from the layout.
     *
     * @param tab
     */
    fun removeTab(tab: Tab) {
        require(!(tab.mParent !== this)) { "Tab does not belong to this TabLayout." }
        removeTabAt(tab.position)
    }

    /**
     * Remove a tab from the layout.
     *
     * @param position
     */
    private fun removeTabAt(position: Int) {
        val selectedTabPosition = mSelectedTab?.position ?: 0
        removeTabViewAt(position)
        val removedTab:Tab? = mTabs.removeAt(position)
        removedTab?.reset()
        val newTabCount = mTabs.size
        for (i in position until newTabCount) {
            mTabs[i].position = i
        }
        if (selectedTabPosition == position) {
            selectTab(if (mTabs.isEmpty()) null else mTabs[Math.max(0, position - 1)])
        }
    }

    /**
     * Remove all tabs from the action bar and deselect the current tab.
     */
    private fun removeAllTabs() { // Remove all the views
        //fix SNS-26574
        mTabStrip.removeAllViews()
        val i = mTabs.iterator()
        while (i.hasNext()) {
            val tab = i.next()
            i.remove()
            tab.reset()
        }
        mSelectedTab = null
    }

    /**
     * tabMode tab的宽度是否适应屏幕
     */
    @get:Mode
    var tabMode: Int
        get() = mMode
        set(mode) {
            if (mode != mMode) {
                mMode = mode
                applyModeAndGravity()
            }
        }

    /**
     * The current gravity used for laying out tabs.
     */
    @get:TabGravity
    var tabGravity: Int
        get() = mTabGravity
        set(gravity) {
            if (mTabGravity != gravity) {
                mTabGravity = gravity
                applyModeAndGravity()
            }
        }

    /**
     * setupWithViewPager
     *
     * @param viewPager
     * @param autoRefresh
     */
    @JvmOverloads
    fun setupWithViewPager(viewPager: ViewPager?, autoRefresh: Boolean = true) {
        setupWithViewPager(viewPager, autoRefresh, false)
    }

    private fun setupWithViewPager(viewPager: ViewPager?, autoRefresh: Boolean,
                                   implicitSetup: Boolean) {
        if (mViewPager != null) { // If we've already been setup with a ViewPager, remove us from it
            mPageChangeListener?.let {
                mViewPager?.removeOnPageChangeListener(it)
            }
            mAdapterChangeListener?.let {
                mViewPager?.removeOnAdapterChangeListener(it)
            }
        }
        mCurrentVpSelectedListener?.let {
            removeOnTabSelectedListener(it)
            mCurrentVpSelectedListener = null
        }
        if (viewPager != null) {
            mViewPager = viewPager
            // Add our custom OnPageChangeListener to the ViewPager
            if (mPageChangeListener == null) {
                mPageChangeListener = TabLayoutOnPageChangeListener(this)
            }
            mPageChangeListener?.reset()
            mPageChangeListener?.let {
                it.reset()
                viewPager.addOnPageChangeListener(it)
            }
            // Now we'll add a tab selected listener to set ViewPager's current item
            mCurrentVpSelectedListener = ViewPagerOnTabSelectedListener(viewPager)
            addOnTabSelectedListener(mCurrentVpSelectedListener as ViewPagerOnTabSelectedListener)
            val adapter = viewPager.adapter
            adapter?.let { setPagerAdapter(it, autoRefresh) }
            // Add a listener so that we're notified of any adapter changes
            if (mAdapterChangeListener == null) {
                mAdapterChangeListener = AdapterChangeListener()
            }
            mAdapterChangeListener?.let {
                it.setAutoRefresh(autoRefresh)
                viewPager.addOnAdapterChangeListener(it)
            }
            // Now update the scroll position to match the ViewPager's current item
            setScrollPosition(viewPager.currentItem, 0f, true)
        } else {
            mViewPager = null
            setPagerAdapter(null, false)
        }
        mSetupViewPagerImplicitly = implicitSetup
    }

    override fun shouldDelayChildPressedState(): Boolean { // Only delay the pressed state if the tabs can scroll
        return tabScrollRange > 0
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mViewPager == null) {
            val vp = parent
            if (vp is ViewPager) {
                setupWithViewPager(vp, autoRefresh = true, implicitSetup = true)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mSetupViewPagerImplicitly) { // If we've been setup with a ViewPager implicitly, let's clear out any listeners, etc
            setupWithViewPager(null)
            mSetupViewPagerImplicitly = false
        }
    }

    private val tabScrollRange: Int
        get() = 0.coerceAtLeast(mTabStrip.width - width - paddingLeft
                - paddingRight)

    /**
     * 设置PagerAdapter
     *
     * @param adapter
     * @param addObserver
     */
    fun setPagerAdapter(adapter: PagerAdapter?, addObserver: Boolean) {
        if (mPagerAdapter != null ) { // If we already have a PagerAdapter, unregister our observer
            mPagerAdapterObserver?.let {
                mPagerAdapter?.unregisterDataSetObserver(it)
            }

        }
        mPagerAdapter = adapter
        if (addObserver && adapter != null) { // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = PagerAdapterObserver()
            }
            mPagerAdapterObserver?.let {
                adapter.registerDataSetObserver(it)
            }

        }
        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter()
    }

    private fun populateFromPagerAdapter() {
        removeAllTabs()
        mPagerAdapter?.run {
            val adapterCount = count
            for (i in 0 until adapterCount) {
                if(this is DynamicTabProvider) {
                    addTab(newTab().setPageTitleItem(getPageTitleItem(i)), false)
                }
            }
            // Make sure we reflect the currently set ViewPager item
            if (mViewPager != null && adapterCount > 0) {
                val curItem = mViewPager?.currentItem ?: 0
                if (curItem != selectedTabPosition && curItem < tabCount) {
                    selectTab(getTabAt(curItem))
                }
            }
        }
    }


    private fun updateAllTabs() {
        var i = 0
        val z = mTabs.size
        while (i < z) {
            mTabs[i].updateView()
            i++
        }
    }

    private fun createTabView(tab: Tab): TabView? {
        var tabView = mTabViewPool?.acquire()
        if (tabView == null) {
            tabView = TabView(context)
        }
        tabView.tab = tab
        tabView.isFocusable = true
        tabView.minimumWidth = tabMinWidth
        return tabView
    }

    private fun configureTab(tab: Tab, position: Int) {
        tab.position = position
        mTabs.add(position, tab)
        val count = mTabs.size
        for (i in position + 1 until count) {
            mTabs[i].position = i
        }
    }

    private fun addTabView(tab: Tab) {
        val tabView = tab.mView
        mTabStrip.addView(tabView, tab.position, createLayoutParamsForTabs())
    }

    override fun addView(child: View) {
        addViewInternal(child)
    }

    override fun addView(child: View, index: Int) {
        addViewInternal(child)
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        addViewInternal(child)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        addViewInternal(child)
    }

    private fun addViewInternal(child: View) {
        addTabFromItemView()
    }

    private fun createLayoutParamsForTabs(): LinearLayout.LayoutParams {
        val lp = LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        updateTabViewLayoutParams(lp)
        return lp
    }

    private fun updateTabViewLayoutParams(lp: LinearLayout.LayoutParams) {
        if (mMode == MODE_FIXED && mTabGravity == GRAVITY_FILL) {
            lp.width = 0
            lp.weight = 1f
        } else {
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT
            lp.weight = 0f
        }
    }

    private fun dpToPx(dps: Int): Int {
        return Math.round(resources.displayMetrics.density * dps)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = getSize(widthMeasureSpec)
        if (getMode(widthMeasureSpec) != UNSPECIFIED) {
            // If we don't have an unspecified width spec, use the given size to calculate the max tab width
            tabMaxWidth = if (mRequestedTabMaxWidth > 0) mRequestedTabMaxWidth else specWidth - dpToPx(TAB_MIN_WIDTH_MARGIN)
        }
        // 高度自适应，由子View来确定StoreTabLayout的高度
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (childCount == 1) {
            // If we're in fixed mode then we need to make the tab strip is the same width as us so we don't scroll
            val child = getChildAt(0)
            var remeasure = false
            when (mMode) {
                MODE_SCROLLABLE ->
                    // We only need to resize the child if it's smaller than us. This is similar to fillViewport
                    remeasure = child.measuredWidth < measuredWidth
                MODE_FIXED ->  // Resize the child so that it doesn't scroll
                    remeasure = child.measuredWidth != measuredWidth
            }
            if (remeasure) { // Re-measure the child with a widthSpec set to be exactly our measure width
                val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, paddingTop
                        + paddingBottom, child.layoutParams.height)
                val childWidthMeasureSpec = makeMeasureSpec(
                    measuredWidth, EXACTLY)
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }

    private fun removeTabViewAt(position: Int) { //        IndexHomeFragment上关注tab使用角标的时候，会把原来tab位置替换成FrameLayout(包含Tab和BadgeView)
        var view: TabView? = null
        if (mTabStrip.getChildAt(position) is TabView) {
            view = mTabStrip.getChildAt(position) as TabView
        } else if (mTabStrip.getChildAt(position) is FrameLayout) { //只针对IndexHomeFragment上的关注Tab
            view = (mTabStrip.getChildAt(position) as FrameLayout).getChildAt(0) as TabView
        }
        mTabStrip.removeViewAt(position)
        if (view != null) {
            view.reset()
            mTabViewPool?.release(view)
        }
        requestLayout()
    }

    private fun animateToTab(newPosition: Int) {
        if (newPosition == Tab.INVALID_POSITION) {
            return
        }
        if (windowToken == null || !ViewCompat.isLaidOut(this)
            || mTabStrip.childrenNeedLayout()) { // If we don't have a window token, or we haven't been laid out yet just draw the new position now
            setScrollPosition(newPosition, 0f, true)
            return
        }
        val startScrollX = scrollX
        val targetScrollX = calculateScrollXForTab(newPosition, 0f)
        if (startScrollX != targetScrollX) {
            ensureScrollAnimator()
            mScrollAnimator?.setIntValues(startScrollX, targetScrollX)
            mScrollAnimator?.start()
        }
        // Now animate the indicator
        mTabStrip.animateIndicatorToPosition(newPosition, ANIMATION_DURATION)
    }

    private fun ensureScrollAnimator() {
        if (mScrollAnimator == null) {
            mScrollAnimator = ValueAnimator()
            mScrollAnimator?.interpolator = AccelerateDecelerateInterpolator()
            mScrollAnimator?.duration = ANIMATION_DURATION.toLong()
            mScrollAnimator?.addUpdateListener { animator -> scrollTo(animator.animatedValue as Int, 0) }
        }
    }

    private fun setScrollAnimatorListener(listener: Animator.AnimatorListener?) {
        ensureScrollAnimator()
        mScrollAnimator?.addListener(listener)
    }

    private fun setSelectedTabView(position: Int) {
        val tabCount = mTabStrip.childCount
        if (position < tabCount) {
            for (i in 0 until tabCount) {
                val child = mTabStrip.getChildAt(i)
                if (child is FrameLayout) {
                    val childCount = child.childCount
                    if (childCount > 0) {
                        child.getChildAt(0).isSelected = i == position
                    }
                } else {
                    child.isSelected = i == position
                }
            }
        }
    }

    @JvmOverloads
    fun selectTab(tab: Tab?, updateIndicator: Boolean = true) {
        val currentTab = mSelectedTab
        if (currentTab == tab) {
            if (currentTab != null) {
                tab?.let {
                    dispatchTabReselected(it)
                    animateToTab(it.position)
                }
            }
        } else {
            val newPosition = tab?.position ?: Tab.INVALID_POSITION
            if (updateIndicator) {
                if ((currentTab == null || currentTab.position == Tab.INVALID_POSITION)
                    && newPosition != Tab.INVALID_POSITION) { // If we don't currently have a tab, just draw the indicator
                    setScrollPosition(newPosition, 0f, true)
                } else {
                    animateToTab(newPosition)
                }
                if (newPosition != Tab.INVALID_POSITION) {
                    setSelectedTabView(newPosition)
                }
            }
            currentTab?.let { dispatchTabUnselected(it) }
            mSelectedTab = tab
            tab?.let { dispatchTabSelected(it) }
        }
    }

    private fun dispatchTabSelected(tab: Tab) {
        for (i in mSelectedListeners.indices.reversed()) {
            mSelectedListeners[i].onTabSelected(tab)
        }
    }

    private fun dispatchTabUnselected(tab: Tab) {
        for (i in mSelectedListeners.indices.reversed()) {
            mSelectedListeners[i].onTabUnselected(tab)
        }
    }

    private fun dispatchTabReselected(tab: Tab) {
        for (i in mSelectedListeners.indices.reversed()) {
            mSelectedListeners[i].onTabReselected(tab)
        }
    }

    private fun calculateScrollXForTab(position: Int, positionOffset: Float): Int {
        if (mMode == MODE_SCROLLABLE) {
            val selectedChild = mTabStrip.getChildAt(position)
            //计算当前需要滑动多少偏移,fix SNS-26590
            val selectedWidth = selectedChild?.width ?: 0
            val newScrollX = selectedChild?.left ?: 0 + selectedWidth/2
            return if(newScrollX > width/2 || newScrollX < -(width/2)) {
                if(newScrollX > 0) {
                    newScrollX - width/2
                } else {
                    newScrollX + width/2
                }
            } else {
                0
            }
        }
        return 0
    }

    private fun applyModeAndGravity() {
        var paddingStart = 0
        if (mMode == MODE_SCROLLABLE) { // If we're scrollable, or fixed at start, inset using padding
            paddingStart = Math.max(0, mContentInsetStart - mTabPaddingStart)
        }
        ViewCompat.setPaddingRelative(mTabStrip, paddingStart, 0, 0, 0)
        when (mMode) {
            MODE_FIXED -> mTabStrip.gravity = Gravity.CENTER_HORIZONTAL
            MODE_SCROLLABLE -> mTabStrip.gravity = GravityCompat.START
        }
        updateTabViews(true)
    }

    private fun updateTabViews(requestLayout: Boolean) {
        for (i in 0 until mTabStrip.childCount) {
            val child = mTabStrip.getChildAt(i)
            child.minimumWidth = tabMinWidth
            updateTabViewLayoutParams(child.layoutParams as LinearLayout.LayoutParams)
            if (requestLayout) {
                child.requestLayout()
            }
        }
    }

    /**
     * A tab in this layout. Instances can be created via [.newTab].
     */
    class Tab internal constructor() {
        /**
         * tag
         */
        var tag: Any? = null
            private set
        var storeTabBean:DynamicTabBean? = null
        /**
         * Return the current position of this tab in the action bar.
         *
         * @return Current position, or [.INVALID_POSITION] if this tab is not currently in
         * the action bar.
         */
        var position = INVALID_POSITION

        var mParent: DynamicTabLayout? = null
        var mView: TabView? = null

        /**
         * Give this Tab an arbitrary object to hold for later use.
         *
         * @param tag Object to store
         * @return The current instance for call chaining
         */
        fun setTag(tag: Any?): Tab {
            this.tag = tag
            return this
        }

        /**
         * 设置Page Title Item数据
         *
         * @param pageTitleItem
         * @return
         */
        fun setPageTitleItem(pageTitleItem: DynamicTabBean?): Tab {
            this.storeTabBean = pageTitleItem
            updateView()
            return this
        }


        /**
         * Select this tab. Only valid if the tab has been added to the action bar.
         */
        fun select() {
            requireNotNull(mParent) { "Tab not attached to a TabLayout" }
            mParent?.selectTab(this)
        }

        /**
         * Returns true if this tab is currently selected.
         */
        val isSelected: Boolean
            get() {
                requireNotNull(mParent) { "Tab not attached to a TabLayout" }
                return mParent?.selectedTabPosition == position
            }

        /**
         * update view
         */
        fun updateView() {
            mView?.update()
        }

        /**
         * reset view
         */
        fun reset() {
            mParent = null
            mView = null
            tag = null
            position = INVALID_POSITION
        }

        companion object {
            /**
             * An invalid position for a tab.
             *
             * @see .getPosition
             */
            const val INVALID_POSITION = -1
        }
    }

    /**
     * TabView
     *
     * @constructor
     * @param context
     */
    inner class TabView(context: Context) : LinearLayout(context) {

        private var mTab: Tab? = null

        private var mStoreTabView:DynamicTabView? = null

        init {
            if (mTabBackgroundResId != 0) {
                ViewCompat.setBackground(
                    this, AppCompatResources.getDrawable(context, mTabBackgroundResId))
            }
            ViewCompat.setPaddingRelative(this, mTabPaddingStart, mTabPaddingTop,
                mTabPaddingEnd, mTabPaddingBottom)
            gravity = Gravity.CENTER
            orientation = VERTICAL
            isClickable = true
            ViewCompat.setPointerIcon(this,
                PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND))
        }

        override fun performClick(): Boolean {
            val handled = super.performClick()
            return if (mTab != null) {
                if (!handled) {
                    playSoundEffect(SoundEffectConstants.CLICK)
                }
                mTab?.select()
                true
            } else {
                handled
            }
        }

        override fun setSelected(selected: Boolean) {
            super.setSelected(selected)
            //更新StoreTabView的选中状态
            mStoreTabView?.setIsSelected(selected)
        }

        /**
         * reset
         */
        fun reset() {
            tab = null
            mStoreTabView = null
            isSelected = false
        }

        /**
         * update
         */
        fun update() {
            val tab = mTab
            val storeTabBean = tab?.storeTabBean

            if(mStoreTabView == null && storeTabBean != null) {
                val storeTabView = createStoreTabView(storeTabBean)
                storeTabView.setup()
                addView(storeTabView)
                mStoreTabView = storeTabView
            }

            // Finally update our selected state
            isSelected = tab != null && tab.isSelected
        }

        private fun createStoreTabView(storeTabBean: DynamicTabBean): DynamicTabView {
            return DynamicTabView(context, title = storeTabBean.title, descText = storeTabBean.desc, tabIcon = storeTabBean.icon)
        }

        /**
         * 设置动画的进度
         *
         * @param progress
         */
        fun changeProgress(progress: Float) {
            mStoreTabView?.changeProgress(progress)
        }

        var tab: Tab?
            get() = mTab
            set(tab) {
                if (tab != mTab) {
                    mTab = tab
                    update()
                }
            }

    }

    /**
     * 下划线
     *
     * @constructor
     * @param context
     */
    private inner class SlidingTabStrip internal constructor(context: Context?) : LinearLayout(context) {
        private var mSelectedIndicatorHeight = 0
        private var mSelectedIndicatorWidth = 0
        private var mSelectedIndicatorOffset = 0
        private val mSelectedIndicatorPaint: Paint
        var mSelectedPosition = -1
        var mSelectionOffset = 0f
        private var mIndicatorLeft = -1
        private var mIndicatorRight = -1
        private var mIndicatorAnimator: ValueAnimator? = null
        private var mSupportIndicatorAnimation = false
        private val mIndicatorRect = RectF()
        private var mIsColorDirty = true
        private var radiusX = 0f
        private var radiusY = 0f


        init {
            setWillNotDraw(false)
            mSelectedIndicatorPaint = Paint()
            mSelectedIndicatorPaint.isAntiAlias = true
            mSelectedIndicatorPaint.strokeCap = Paint.Cap.ROUND
        }

        /**
         * 设置Indicator Color
         *
         * @param color
         */
        fun setSelectedIndicatorColor(color: Int) {
            if (mSelectedIndicatorPaint.color != color) {
                mSelectedIndicatorPaint.color = color
                mIsColorDirty = true
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

        /**
         * 设置Indicator height
         *
         * @param height
         */
        fun setSelectedIndicatorHeight(height: Int) {
            if (mSelectedIndicatorHeight != height) {
                mSelectedIndicatorHeight = height
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

        /**
         * 设置Indicator width
         *
         * @param width
         */
        fun setSelectedIndicatorWidth(width: Int) {
            if (mSelectedIndicatorWidth != width) {
                mSelectedIndicatorWidth = width
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

        /**
         * 设置Indicator offset
         *
         * @param offset
         */
        fun setSelectedIndicatorOffset(offset: Int) {
            if (mSelectedIndicatorOffset != offset) {
                mSelectedIndicatorOffset = offset
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

        /**
         * 是否支持动画
         *
         * @param supportTabIndicatorAnimation
         */
        fun setSupportTabIndicatorAnimation(supportTabIndicatorAnimation: Boolean) {
            mSupportIndicatorAnimation = supportTabIndicatorAnimation
        }

        /**
         * 设置Indicator rx
         *
         * @param rx
         */
        fun setIndicatorRoundRadiusX(rx: Float) {
            radiusX = rx
        }

        /**
         * 设置Indicator ry
         *
         * @param ry
         */
        fun setIndicatorRoundRadiusY(ry: Float) {
            radiusY = ry
        }

        /**
         * childrenNeedLayout
         *
         * @return
         */
        fun childrenNeedLayout(): Boolean {
            var i = 0
            val z = childCount
            while (i < z) {
                val child = getChildAt(i)
                if (child.width <= 0) {
                    return true
                }
                i++
            }
            return false
        }

        /**
         * 设置Indicator的position
         *
         * @param position
         * @param positionOffset
         */
        fun setIndicatorPositionFromTabPosition(position: Int, positionOffset: Float) {
            if (mIndicatorAnimator != null && mIndicatorAnimator?.isRunning == true) {
                mIndicatorAnimator?.cancel()
            }
            mSelectedPosition = position
            mSelectionOffset = positionOffset
            updateIndicatorPosition()
        }

        val indicatorPosition: Float
            get() = mSelectedPosition + mSelectionOffset

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            if (getMode(widthMeasureSpec) != EXACTLY) {
                // HorizontalScrollView will first measure use with UNSPECIFIED, and then with EXACTLY. Ignore the first call since anything we do will be overwritten anyway
                return
            }
            if (mMode == MODE_FIXED && mTabGravity == GRAVITY_CENTER) {
                val count = childCount
                // First we'll find the widest tab
                var largestTabWidth = 0
                var i = 0
                while (i < count) {
                    val child = getChildAt(i)
                    if (child.visibility == View.VISIBLE) {
                        largestTabWidth = Math.max(largestTabWidth, child.measuredWidth)
                    }
                    i++
                }
                if (largestTabWidth <= 0) { // If we don't have a largest child yet, skip until the next measure pass
                    return
                }
                val gutter = dpToPx(FIXED_WRAP_GUTTER_MIN)
                var remeasure = false
                if (largestTabWidth * count <= measuredWidth - gutter * 2) {
                    // If the tabs fit within our width minus gutters, we will set all tabs to have the same width
                    for (i in 0 until count) {
                        val lp = getChildAt(i).layoutParams as LayoutParams
                        if (lp.width != largestTabWidth || lp.weight != 0f) {
                            lp.width = largestTabWidth
                            lp.weight = 0f
                            remeasure = true
                        }
                    }
                } else {
                    // If the tabs will wrap to be larger than the width minus gutters, we need to switch to GRAVITY_FILL
                    mTabGravity = GRAVITY_FILL
                    updateTabViews(false)
                    remeasure = true
                }
                if (remeasure) { // Now re-measure after our changes
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                }
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)
            if (mIndicatorAnimator != null && mIndicatorAnimator?.isRunning == true) {
                // If we're currently running an animation, lets cancel it and start a new animation with the remaining duration
                mIndicatorAnimator?.cancel()
                val duration = mIndicatorAnimator?.duration ?: 1L
                animateIndicatorToPosition(mSelectedPosition,
                    Math.round((1f - (mIndicatorAnimator?.animatedFraction?:0f)) * duration))
            } else { // If we've been layed out, update the indicator position
                updateIndicatorPosition()
            }
        }

        /**
         * 默认改了Indicator宽度后更新Indicator Position的方法
         */
        private fun updateIndicatorPosition() {
            val selectedTitle = getChildAt(mSelectedPosition)
            var left: Int
            var right: Int
            if (selectedTitle != null && selectedTitle.width > 0) {
                left = selectedTitle.left + (selectedTitle.width - mSelectedIndicatorWidth) / 2
                right = left + mSelectedIndicatorWidth
                if (mSelectionOffset > 0f && mSelectedPosition < childCount - 1) { // Draw the selection partway between the tabs
                    val nextTitle = getChildAt(mSelectedPosition + 1)
                    //是否支持动画，支持动画主要这里的处理不一样
                    if (mSupportIndicatorAnimation) {
                        val nextTitleLeft = nextTitle.left + (nextTitle.width - mSelectedIndicatorWidth) / 2
                        if (mSelectionOffset <= 0.5f) {
                            left = selectedTitle.left + (selectedTitle.width - mSelectedIndicatorWidth) / 2
                            right = lerp(right, nextTitleLeft + mSelectedIndicatorWidth, mSelectionOffset * 2)
                        } else {
                            left = lerp(left, nextTitleLeft, (mSelectionOffset - 0.5f) * 2)
                            right = nextTitleLeft + mSelectedIndicatorWidth
                        }
                    } else { // Draw the selection partway between the tabs
                        left = (mSelectionOffset * (nextTitle.left + (nextTitle.width - mSelectedIndicatorWidth) / 2)
                                + (1.0f - mSelectionOffset) * left).toInt()
                        right = left + mSelectedIndicatorWidth
                    }
                }
            } else {
                right = -1
                left = right
            }
            setIndicatorPosition(left, right)
        }

        /**
         * setIndicatorPosition
         *
         * @param left
         * @param right
         */
        fun setIndicatorPosition(left: Int, right: Int) {
            if (left != mIndicatorLeft || right != mIndicatorRight) { // If the indicator's left/right has changed, invalidate
                mIndicatorLeft = left
                mIndicatorRight = right
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

        private fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
            return startValue + Math.round(fraction * (endValue - startValue))
        }

        /**
         * 动画移动到某个tab
         *
         * @param position
         * @param duration
         */
        fun animateIndicatorToPosition(position: Int, duration: Int) {
            if (mIndicatorAnimator != null && mIndicatorAnimator?.isRunning == true) {
                mIndicatorAnimator?.cancel()
            }
            val targetView = getChildAt(position)
            if (targetView == null) { // If we don't have a view, just update the position now and return
                updateIndicatorPosition()
                return
            }
            val targetLeft = targetView.left + (targetView.width - mSelectedIndicatorWidth) / 2
            val targetRight = targetLeft + mSelectedIndicatorWidth
            val startLeft: Int
            val startRight: Int
            if (Math.abs(position - mSelectedPosition) <= 1) { // If the views are adjacent, we'll animate from edge-to-edge
                startLeft = mIndicatorLeft
                startRight = mIndicatorRight
            } else { // Else, we'll just grow from the nearest edge
                val offset = dpToPx(MOTION_NON_ADJACENT_OFFSET)
                if (position < mSelectedPosition) { // We're going end-to-start
                    startRight = targetRight + offset
                    startLeft = startRight
                } else { // We're going start-to-end
                    startRight = targetLeft - offset
                    startLeft = startRight
                }
            }
            if (startLeft != targetLeft || startRight != targetRight) {
                mIndicatorAnimator = ValueAnimator()
                val animator = mIndicatorAnimator!!
                animator.interpolator = AccelerateDecelerateInterpolator()
                animator.duration = duration.toLong()
                animator.setFloatValues(0f, 1f)
                animator.addUpdateListener { animator ->
                    val fraction = animator.animatedFraction
                    if (mSupportIndicatorAnimation) {
                        val left: Int
                        val right: Int
                        if (mSelectedPosition < position) {
                            if (fraction <= 0.5f) {
                                left = startLeft
                                right = lerp(startRight, targetRight, fraction * 2)
                            } else {
                                left = lerp(startLeft, targetLeft, (fraction - 0.5f) * 2)
                                right = targetRight
                            }
                        } else {
                            if (fraction <= 0.5f) {
                                left = lerp(startLeft, targetLeft, fraction * 2)
                                right = startRight
                            } else {
                                left = targetLeft
                                right = lerp(startRight, targetRight, (fraction - 0.5f) * 2)
                            }
                        }
                        setIndicatorPosition(left, right)
                    } else {
                        setIndicatorPosition(
                            lerp(startLeft, targetLeft, fraction),
                            lerp(startRight, targetRight, fraction))
                    }
                }
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animator: Animator) {
                        mSelectedPosition = position
                        mSelectionOffset = 0f
                    }
                })
                animator.start()
            }
        }

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            var left = mIndicatorLeft
            var right = mIndicatorRight
            // get edges of
            if (mSelectionOffset > 0 && mSelectedPosition < childCount - 1) {
                val leftView = getChildAt(mSelectedPosition)
                val rightView = getChildAt(mSelectedPosition + 1)
                left = leftView.left
                right = rightView.right
            }
            // ensure color updated
            if (mSelectedIndicatorPaint.shader == null || mIsColorDirty) {
                val gradient = LinearGradient(0f, 0f, width.toFloat(), 0f, mSelectedIndicatorPaint.color, mSelectedIndicatorPaint.color, Shader.TileMode.CLAMP)
                mSelectedIndicatorPaint.shader = gradient
            }
            // visible rect

            mIndicatorRect[mIndicatorLeft - dpToPx(2)- mSelectedIndicatorOffset.toFloat(), height - mSelectedIndicatorHeight.toFloat(), mIndicatorRight - dpToPx(2) - mSelectedIndicatorOffset.toFloat()] = height.toFloat()
            // show dst round rect only, but with src background
            val sc = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
            // draw dst round rect
            canvas.drawRoundRect(mIndicatorRect, radiusX, radiusY, mSelectedIndicatorPaint)
            mSelectedIndicatorPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            // draw src background on
            canvas.drawRect(left - mSelectedIndicatorOffset.toFloat(), height - mSelectedIndicatorHeight.toFloat(),
                right - mSelectedIndicatorOffset.toFloat(), height.toFloat(), mSelectedIndicatorPaint)
            mSelectedIndicatorPaint.xfermode = null
            canvas.restoreToCount(sc)
        }

    }

    // If we have been given a min width, use it Else, we'll use the default value
    private val tabMinWidth: Int
        get() {
            if (mRequestedTabMinWidth != INVALID_WIDTH) { // If we have been given a min width, use it
                return mRequestedTabMinWidth
            }
            // Else, we'll use the default value
            return if (mMode == MODE_SCROLLABLE) mScrollableTabMinWidth else 0
        }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return generateDefaultLayoutParams()
    }

    /**
     * StoreTabLayout的监听
     *
     * @constructor
     * @param tabLayout
     */
    class TabLayoutOnPageChangeListener(tabLayout: DynamicTabLayout) : ViewPager.OnPageChangeListener {
        private val mTabLayoutRef: WeakReference<DynamicTabLayout> = WeakReference(tabLayout)
        private var mPreviousScrollState = 0
        private var mScrollState = 0
        override fun onPageScrollStateChanged(state: Int) {
            mPreviousScrollState = mScrollState
            mScrollState = state
        }

        override fun onPageScrolled(position: Int, positionOffset: Float,
                                    positionOffsetPixels: Int) {
            val tabLayout = mTabLayoutRef.get()
            if (tabLayout != null) {
                val updateText = mScrollState != ViewPager.SCROLL_STATE_SETTLING ||
                        mPreviousScrollState == ViewPager.SCROLL_STATE_DRAGGING
                val updateIndicator = !(mScrollState == ViewPager.SCROLL_STATE_SETTLING
                        && mPreviousScrollState == ViewPager.SCROLL_STATE_IDLE)
                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator)
            }
        }

        override fun onPageSelected(position: Int) {
            val tabLayout = mTabLayoutRef.get()
            if (tabLayout != null && tabLayout.selectedTabPosition != position && position < tabLayout.tabCount) {
                val updateIndicator = (mScrollState == ViewPager.SCROLL_STATE_IDLE
                        || (mScrollState == ViewPager.SCROLL_STATE_SETTLING
                        && mPreviousScrollState == ViewPager.SCROLL_STATE_IDLE))
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator)
            }
        }

        /**
         * reset
         */
        fun reset() {
            mScrollState = ViewPager.SCROLL_STATE_IDLE
            mPreviousScrollState = mScrollState
        }

    }

    /**
     * ViewPagerOnTabSelectedListener
     *
     * @property mViewPager
     */
    class ViewPagerOnTabSelectedListener(private val mViewPager: ViewPager) : OnTabSelectedListener {
        override fun onTabSelected(tab: Tab) {
            mViewPager.currentItem = tab.position
        }

        override fun onTabUnselected(tab: Tab?) { // No-op
        }

        override fun onTabReselected(tab: Tab?) { // No-op
        }

    }

    /**
     * PagerAdapterObserver
     */
    private inner class PagerAdapterObserver internal constructor() : DataSetObserver() {
        override fun onChanged() {
            populateFromPagerAdapter()
        }

        override fun onInvalidated() {
            populateFromPagerAdapter()
        }
    }

    /**
     * AdapterChangeListener
     */
    private inner class AdapterChangeListener internal constructor() : ViewPager.OnAdapterChangeListener {
        private var mAutoRefresh = false
        override fun onAdapterChanged(viewPager: ViewPager,
                                      oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?) {
            if (mViewPager === viewPager) {
                setPagerAdapter(newAdapter, mAutoRefresh)
            }
        }

        /**
         * 设置是否支持自动刷新
         *
         * @param autoRefresh
         */
        fun setAutoRefresh(autoRefresh: Boolean) {
            mAutoRefresh = autoRefresh
        }
    }

    companion object {
        private const val INVALID_WIDTH = -1
        private const val DEFAULT_HEIGHT = 48 // dps
        private const val TAB_MIN_WIDTH_MARGIN = 56 //dps
        const val FIXED_WRAP_GUTTER_MIN = 16 //dps
        const val MOTION_NON_ADJACENT_OFFSET = 24
        private const val ANIMATION_DURATION = 300
        /**
         * 可滑动的
         */
        const val MODE_SCROLLABLE = 0
        /**
         * 固定宽度，适应屏幕宽度
         */
        const val MODE_FIXED = 1
        /**
         * Gravity used to fill the [StoreTabLayout] as much as possible. This option only takes effect
         * when used with [.MODE_FIXED].
         *
         * @see .setTabGravity
         * @see .getTabGravity
         */
        const val GRAVITY_FILL = 0
        /**
         * Gravity used to lay out the tabs in the center of the [StoreTabLayout].
         *
         * @see .setTabGravity
         * @see .getTabGravity
         */
        const val GRAVITY_CENTER = 1
    }

}