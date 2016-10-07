package org.thehellnet.shab.mobile.service.location.nmea;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.joda.time.format.DateTimeFormat;
import org.thehellnet.shab.protocol.helper.GpsFixQuality;

/**
 * Created by sardylan on 14/06/16.
 */
public class NmeaListener implements GpsStatus.NmeaListener {

    private static final String TAG = NmeaListener.class.getSimpleName();

    private Context context = new Context();
    private NmeaCallback callback;

    public NmeaListener(NmeaCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        if (nmea == null || nmea.length() == 0) {
            return;
        }

        Log.i(TAG, String.format("New NMEA sentence: %s", nmea));

        String[] items = nmea.split(",");

        switch (items[0]) {
            case "$GPGGA":
                parseGPGGA(items);
                break;
            case "$GPRMC":
                parseGPRMC(items);
                break;
            case "$GPGSA":
                parseGPGSA(items);
                break;
            case "$GPGSV":
                parseGPGSV(items);
                break;
            case "$GPVTG":
                parseGPVTG(items);
                break;

            case "$GNGNS":
                parseGNGNS(items);
                break;
            case "$GNGSA":
                parseGNGSA(items);
                break;

            case "$GLGSV":
                parseGLGSV(items);
                break;
        }
    }

    private void parseGPGGA(String[] items) {
        if (items[1].length() > 0)
            context.dateTime = Utility.dateTimeFromTimeString(context.dateTime, items[1]);

        if (items[2].length() > 0 && items[3].length() > 0)
            context.latitude = Utility.parseLatitude(items[2], items[3]);

        if (items[4].length() > 0 && items[5].length() > 0)
            context.longitude = Utility.parseLongitude(items[4], items[5]);

        if (items[6].length() > 0)
            context.fixQuality = GpsFixQuality.fromNumber(Integer.parseInt(items[6]));

        if (items[7].length() > 0)
            context.satellites = Integer.parseInt(items[7]);

        if (items[8].length() > 0)
            context.HDOP = Float.parseFloat(items[8]);

        if (items[9].length() > 0 && items[10].length() > 0)
            context.altitude = Double.parseDouble(items[9]);

        if (items[11].length() > 0 && items[12].length() > 0)
            context.geoid = Double.parseDouble(items[11]);

        if (items[6].length() > 0 && context.fixQuality != GpsFixQuality.INVALID)
            callback.onLocationChanged(toLocation());
    }

    private void parseGPGSA(String[] items) {
        if (items[14].length() > 0)
            context.PDOP = Float.parseFloat(items[14]);
        if (items[15].length() > 0)
            context.HDOP = Float.parseFloat(items[15]);
        if (items[16].length() > 0)
            context.VDOP = Float.parseFloat(items[16]);
    }

    private void parseGPRMC(String[] items) {
        if (items[1].length() > 0 || items[9].length() > 0) {
            String dateTimeString = items[9] + items[1].split("\\.")[0];
            if (dateTimeString.length() == 12) {
                context.dateTime = DateTimeFormat.forPattern("ddMMyyHHmmss").parseDateTime(dateTimeString);
            } else if (dateTimeString.length() == 14) {
                context.dateTime = DateTimeFormat.forPattern("yyyyMMddHHmmss").parseDateTime(dateTimeString);
            }
        }
        if (items[3].length() > 0 && items[4].length() > 0)
            context.latitude = Utility.parseLatitude(items[3], items[4]);
        if (items[5].length() > 0 && items[6].length() > 0)
            context.longitude = Utility.parseLongitude(items[5], items[6]);
        if (items[7].length() > 0)
            context.speed = Float.parseFloat(items[7]);
        if (items[8].length() > 0)
            context.bearing = Float.parseFloat(items[8]);
    }

    private void parseGPGSV(String[] items) {

    }

    private void parseGPVTG(String[] items) {

    }

    private void parseGNGNS(String[] items) {

    }

    private void parseGNGSA(String[] items) {

    }

    private void parseGLGSV(String[] items) {

    }

    private Location toLocation() {
        Bundle extras = new Bundle();
        extras.putString("fixQuality", context.fixQuality.toString());

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(context.latitude);
        location.setLongitude(context.longitude);
        location.setAltitude(context.altitude);
        location.setSpeed(context.speed);
        location.setBearing(context.bearing);
        location.setTime(context.dateTime.getMillis());
        location.setExtras(extras);
        return location;
    }
}
