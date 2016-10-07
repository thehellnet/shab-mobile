package org.thehellnet.shab.mobile.service.location;

import android.location.Location;

/**
 * Created by sardylan on 26/09/16.
 */

public interface LocationCallback {

    void newLocation(Location location);

    void newGpsStatus(String gpsStatus);
}
