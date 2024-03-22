package com.gaohui.nestedrecyclerview.update.listener;


/**
 * @author tangmingjian
 * @version v1.0
 * @date 2023/5/4
 * @description 首页滚动状态监听
 */
public interface IHPScrollListener {
    int SCROLL_FROM_HP = 0;
    int SCROLL_FROM_JFY = 1;

    /**
     * 首页滚动状态监听
     *
     * @param from  首页或者jfy
     *              {@link #SCROLL_FROM_HP}
     *              {@link #SCROLL_FROM_JFY}
     * @param state 滚动状态
     */
    void onScrollStateChanged(int from, int state);
}
