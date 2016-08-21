package org.thehellnet.shab.mobile.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by sardylan on 21/08/16.
 */

public final class ImageManipulation {

    public static Bitmap createBitmap(byte[] rawData) {
        return BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
    }
}
