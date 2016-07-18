package org.thehellnet.shab.mobile.service.protocol;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.thehellnet.shab.mobile.SHAB;
import org.thehellnet.shab.mobile.config.P;
import org.thehellnet.shab.mobile.config.Prefs;
import org.thehellnet.shab.mobile.utility.DeviceIdentifier;

import java.util.Locale;

/**
 * Created by sardylan on 17/07/16.
 */
public final class Protocol {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHMMSS");
    private static final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SHAB.getAppContext());

    public static String clientCommand() {
        return String.format(Locale.getDefault(), "%s|%s|%s|%s",
                P.CLIENT,
                DeviceIdentifier.getDeviceId(),
                prefs.getString(Prefs.NAME, ""),
                DATE_TIME_FORMATTER.print(new DateTime()));
    }

    public static String clientCommand(Location location) {
        return String.format(Locale.getDefault(), "%s|%.06f|%.06f|%.06f",
                clientCommand(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
    }
}
