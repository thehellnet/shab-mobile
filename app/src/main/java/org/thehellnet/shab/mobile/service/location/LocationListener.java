package org.thehellnet.shab.mobile.service.location;

import android.location.Location;
import android.os.Bundle;

/**
 * Created by sardylan on 17/07/16.
 */
public abstract class LocationListener implements android.location.LocationListener {

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public abstract void onLocationChanged(Location location);
}
