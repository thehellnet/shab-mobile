package org.thehellnet.shab.mobile.utility;

import java.util.Locale;

/**
 * Created by sardylan on 21/08/16.
 */

public final class Formatter {

    public static String coordinateToString(double coordinate) {
        return String.format(Locale.US, "%.06f", coordinate);
    }

    public static String temperatureToString(float temp) {
        return String.format(Locale.US, "%.01f", temp);
    }
}
