package com.charles.funmusic.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 工具类
 */
public class ScreenUtil {
    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static int getScreenWidth() {
        WindowManager windowManager = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(dm);
        }
        return dm.widthPixels;
    }

    public static int getScreenHeight() {
        WindowManager windowManager = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(dm);
        }
        return dm.heightPixels;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = sContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = sContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int dp2px(float dpValue) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(float pxValue) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = sContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
