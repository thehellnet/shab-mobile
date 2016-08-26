package org.thehellnet.shab.mobile.utility;

import java.util.Locale;

/**
 * Created by sardylan on 21/08/16.
 */

public final class Formatter {

    public static String latitudeToString(double latitude) {
        String letter = latitude >= 0 ? "N" : "S";
        return String.format(Locale.US, "%.06f %s", latitude, letter);
    }

    public static String longitudeToString(double longitude) {
        String letter = longitude >= 0 ? "E" : "W";
        return String.format(Locale.US, "%.06f %s", longitude, letter);
    }

    public static String altitudeToString(double altitude) {
        return String.format(Locale.US, "%.01f m", altitude);
    }

    public static String temperatureToString(float temp) {
        return String.format(Locale.US, "%.01f °C", temp);
    }
}
