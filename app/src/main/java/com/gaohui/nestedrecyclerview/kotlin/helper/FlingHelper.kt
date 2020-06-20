package com.gaohui.nestedrecyclerview.kotlin.helper

import android.content.Context
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

/**
 * Fling 帮助类
 */
class FlingHelper(context: Context) {

    private var mPhysicalCoeff: Float = context.resources.displayMetrics.density * 160.0f * 386.0878f * 0.84f

    private fun getSplineDeceleration(i: Int): Double {
        return ln((0.35f * abs(i).toFloat() / (mFlingFriction * mPhysicalCoeff)).toDouble())
    }

    private fun getSplineDecelerationByDistance(d: Double): Double {
        return (DECELERATION_RATE.toDouble() - 1.0) * ln(d / (mFlingFriction * mPhysicalCoeff).toDouble()) / DECELERATION_RATE.toDouble()
    }

    /**
     * 根据加速度来获取需要fling的距离
     * @param i 加速度
     * @return fling的距离
     */
    fun getSplineFlingDistance(i: Int): Double {
        return exp(getSplineDeceleration(i) * (DECELERATION_RATE.toDouble() / (DECELERATION_RATE.toDouble() - 1.0))) * (mFlingFriction * mPhysicalCoeff).toDouble()
    }

    /**
     * 根据距离来获取加速度
     * @param d 距离
     * @return 返回加速度
     */
    fun getVelocityByDistance(d: Double): Int {
        return abs((exp(getSplineDecelerationByDistance(d)) * mFlingFriction.toDouble() * mPhysicalCoeff.toDouble() / 0.3499999940395355).toInt())
    }

    companion object {
        private val DECELERATION_RATE = (ln(0.78) / ln(0.9)).toFloat()
        private val mFlingFriction = ViewConfiguration.getScrollFriction()

    }
}
