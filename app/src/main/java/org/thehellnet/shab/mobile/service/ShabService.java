package org.thehellnet.shab.mobile.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.thehellnet.shab.mobile.config.Prefs;
import org.thehellnet.shab.mobile.location.LocationListener;
import org.thehellnet.shab.mobile.protocol.Protocol;
import org.thehellnet.shab.mobile.protocol.ShabSocket;
import org.thehellnet.shab.mobile.protocol.ShabSocketCallback;

/**
 * Created by sardylan on 16/07/16.
 */
public class ShabService extends Service implements ShabSocketCallback {

    private static final String TAG = ShabService.class.getSimpleName();
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private final Object SYNC_START = new Object();

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location lastLocation;

    private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    private ShabSocket shabSocket;
    private boolean alreadyStarted = false;

    public ShabService() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                shabSocket.send(Protocol.clientCommand(lastLocation));
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        synchronized (SYNC_START) {
            if (!alreadyStarted) {
                start();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stop();
        super.onDestroy();
    }

    @Override
    public void connected() {
        shabSocket.send(Protocol.clientCommand());
    }

    @Override
    public void newLine(String line) {
        Log.i(TAG, String.format("New line from socket: %s", line));
    }

    @Override
    public void disconnected() {
        stop();
    }

    private void start() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);
        }

        shabSocket = new ShabSocket(this);
        shabSocket.start(prefs.getString(Prefs.SERVER_ADDRESS, Prefs.SERVER_ADDRESS_DEFAULT),
                prefs.getInt(Prefs.SOCKET_PORT, Prefs.SOCKET_PORT_DEFAULT));
        alreadyStarted = true;
    }

    private void stop() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }

        shabSocket.stop();
        shabSocket = null;
        alreadyStarted = false;
    }
}
