package org.thehellnet.shab.mobile.utility;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import org.thehellnet.shab.mobile.SHAB;

/**
 * Created by sardylan on 21/08/16.
 */

public final class ScreenConverter {

    public static int dpToPixel(int dp) {
        Resources resources = SHAB.getAppContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static float pixelToDp(int px) {
        Resources resources = SHAB.getAppContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
