package com.gaohui.nestedrecyclerview.kotlin

/**
 * VisibleChang回调
 */
interface OnUserVisibleChange {
    /**
     * 是否对用户可见
     *
     * @param isVisibleToUser
     */
    fun onUserVisibleChange(isVisibleToUser:Boolean)
}