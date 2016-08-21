package org.thehellnet.shab.mobile.utility;

import android.content.res.Resources;
import android.util.TypedValue;

import org.thehellnet.shab.mobile.SHAB;

/**
 * Created by sardylan on 21/08/16.
 */

public final class ScreenConverter {

    public static int dpToPixel(int dp) {
        Resources resources = SHAB.getAppContext().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
