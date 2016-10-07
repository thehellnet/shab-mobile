package org.thehellnet.shab.mobile.service.location;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.joda.time.DateTime;
import org.thehellnet.shab.mobile.SHAB;
import org.thehellnet.shab.mobile.protocol.ShabContext;
import org.thehellnet.shab.mobile.service.location.nmea.NmeaCallback;
import org.thehellnet.shab.mobile.service.location.nmea.NmeaListener;

/**
 * Created by sardylan on 26/09/16.
 */

public class LocationService extends Service implements LocationProvider {

    public class ServiceBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }
    }

    private class PassiveLocationListener extends LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged PASSIVE");
        }
    }

    private class NetworkLocationListener extends LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged NETWORK");
            networkLocation = location;
            parseUpdate();
        }
    }

    private class GpsLocationListener extends LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged GPS");
        }
    }

    private class GpsNmeaCallback implements NmeaCallback {

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged GPS");
            gpsLocation = location;
            gpsDateTime = new DateTime();
            parseUpdate();
        }
    }

    private static final String TAG = LocationService.class.getSimpleName();
    private static final int LOCATION_INTERVAL = 5000;
    private static final float LOCATION_DISTANCE = .01f;
    private static final int POSITION_TIMEOUT_GPS = 10;
    private static final int POSITION_UPDATE_INTERVAL = 2;
    private static final String GPS_STATUS_NETWORK = "Network";

    private final IBinder serviceBinder = new ServiceBinder();

    private LocationCallback callback;
    private LocationManager locationManager = (LocationManager) SHAB.getAppContext().getSystemService(Context.LOCATION_SERVICE);

    private LocationListener passiveLocationListener = new PassiveLocationListener();
    private LocationListener networkLocationListener = new NetworkLocationListener();
    private LocationListener gpsLocationListener = new GpsLocationListener();

    private GpsNmeaCallback nmeaCallback = new GpsNmeaCallback();
    private NmeaListener nmeaListener = new NmeaListener(nmeaCallback);

    private ShabContext shabContext = ShabContext.getInstance();

    private Location networkLocation;
    private Location gpsLocation;

    private DateTime gpsDateTime;
    private DateTime lastDateTime = new DateTime();

    private String gpsStatus;

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void setCallback(LocationCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        start();
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }

    private void start() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, passiveLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, networkLocationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, gpsLocationListener);
            locationManager.addNmeaListener(nmeaListener);
        }
    }

    private void stop() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(passiveLocationListener);
            locationManager.removeUpdates(networkLocationListener);
            locationManager.removeUpdates(gpsLocationListener);
            locationManager.removeNmeaListener(nmeaListener);
        }
    }

    private void parseUpdate() {
        if (gpsDateTime != null && gpsDateTime.plusSeconds(POSITION_TIMEOUT_GPS).isBeforeNow()) {
            gpsLocation = null;
            gpsDateTime = null;
        }

        if (lastDateTime.plusSeconds(POSITION_UPDATE_INTERVAL).isAfterNow()) {
            return;
        }
        lastDateTime = new DateTime();

        if (gpsLocation != null) {
            updateGpsStatus(gpsLocation.getExtras().getString("fixQuality"));
            callback.newLocation(gpsLocation);
        } else {
            updateGpsStatus(GPS_STATUS_NETWORK);
            callback.newLocation(networkLocation);
        }
    }

    private synchronized void updateGpsStatus(String newGpsStatus) {
        if (!newGpsStatus.equals(gpsStatus)) {
            gpsStatus = newGpsStatus;
            callback.newGpsStatus(gpsStatus);
        }
    }
}
