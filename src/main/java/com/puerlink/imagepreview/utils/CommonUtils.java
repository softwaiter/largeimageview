package com.puerlink.imagepreview.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by wangxm on 2016/8/27.
 */
public class CommonUtils {

    public static int dp2px(Context context, int dip)
    {
        Resources resources = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics());
        return Math.round(px);
    }

}
