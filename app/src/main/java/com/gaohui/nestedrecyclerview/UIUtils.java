package com.gaohui.nestedrecyclerview;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class UIUtils {
    private static final DisplayMetrics sMetrics = Resources.getSystem().getDisplayMetrics();

    public static int getScreenWidth() {
        return sMetrics != null ? sMetrics.widthPixels : 0;
    }

    public static int getScreenHeight() {
        return sMetrics != null ? sMetrics.heightPixels : 0;
    }


    public static int px2dp(float pxValue) {
        final float scale = sMetrics != null ? sMetrics.density : 1;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dp2px(float dipValue) {
        final float scale = sMetrics != null ? sMetrics.density : 1;
        return (int) (dipValue * scale + 0.5f);
    }


    public static int px2sp(float pxValue) {
        final float fontScale = sMetrics != null ? sMetrics.scaledDensity : 1;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = sMetrics != null ? sMetrics.scaledDensity : 1;
        return (int) (spValue * fontScale + 0.5f);
    }

}

