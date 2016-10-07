package org.thehellnet.shab.mobile.service.location.nmea;

import android.location.Location;

/**
 * Created by sardylan on 27/09/16.
 */

public interface NmeaCallback {

    void onLocationChanged(Location location);
}
