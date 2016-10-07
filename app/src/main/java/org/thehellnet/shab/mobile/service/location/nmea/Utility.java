package org.thehellnet.shab.mobile.service.location.nmea;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Created by sardylan on 03/10/16.
 */

class Utility {

    static DateTime dateTimeFromTimeString(DateTime oldDateTime, String newTime) {
        return new DateTime(oldDateTime.getYear(),
                oldDateTime.getMonthOfYear(),
                oldDateTime.getDayOfMonth(),
                Integer.parseInt(newTime.substring(0, 2)),
                Integer.parseInt(newTime.substring(2, 4)),
                Integer.parseInt(newTime.substring(4, 6)),
                DateTimeZone.UTC);
    }

    static double parseLatitude(String latitude, String northSouth) {
        double value = Double.parseDouble(latitude.substring(0, 2))
                + (Double.parseDouble(latitude.substring(2)) / 60f);
        if (northSouth.toUpperCase().equals("S")) value *= -1;
        return value;
    }

    static double parseLongitude(String latitude, String northSouth) {
        double value = Double.parseDouble(latitude.substring(0, 3))
                + (Double.parseDouble(latitude.substring(3)) / 60f);
        if (northSouth.toUpperCase().equals("W")) value *= -1;
        return value;
    }
}
