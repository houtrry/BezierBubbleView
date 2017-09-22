package com.houtrry.bezierbubbleview;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/22 15:45
 * @desc ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public class DisplayUtils {

    /**
     * px 转dp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final float density = displayMetrics.density;
        return (int) (pxValue / density + 0.5f);//四舍五入
    }

    /**
     * dp转px
     * @param context
     * @param dpValue
     * @return
     */
    public static int dp2px(Context context, float dpValue) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final float density = displayMetrics.density;
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * px转sp
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final float scaledDensity = displayMetrics.scaledDensity;
        return (int) (pxValue / scaledDensity + 0.5f);
    }

    /**
     * sp转px
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final float scaledDensity = displayMetrics.scaledDensity;
        return (int) (spValue * scaledDensity + 0.5f);
    }
}
