package org.thehellnet.shab.mobile.service.location.nmea;

import org.joda.time.DateTime;
import org.thehellnet.shab.protocol.helper.GpsFixQuality;

/**
 * Created by sardylan on 03/10/16.
 */

class Context {
    DateTime dateTime = new DateTime();
    double latitude = 0;
    double longitude = 0;
    double altitude = 0;
    GpsFixQuality fixQuality = GpsFixQuality.INVALID;
    int satellites = 0;
    double geoid = 0;
    float speed = 0;
    float bearing = 0;
    float HDOP = 0;
    float VDOP = 0;
    float PDOP = 0;
}
