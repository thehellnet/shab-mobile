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

import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.mobile.config.Prefs;
import org.thehellnet.shab.mobile.location.LocationListener;
import org.thehellnet.shab.protocol.DataKeeper;
import org.thehellnet.shab.protocol.bean.Client;
import org.thehellnet.shab.protocol.command.ClientUpdate;
import org.thehellnet.shab.protocol.command.Command;
import org.thehellnet.shab.protocol.command.Parser;
import org.thehellnet.shab.protocol.socket.ShabSocket;
import org.thehellnet.shab.protocol.socket.ShabSocketCallback;

/**
 * Created by sardylan on 16/07/16.
 */
public class ShabService extends Service implements ShabSocketCallback {

    private class GpsLocationListener extends LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
            dataKeeper.getClient().setPosition(lastLocation.getLatitude(),
                    lastLocation.getLongitude(),
                    lastLocation.getAltitude());
            shabSocket.send(ClientUpdate.serialize(dataKeeper.getClient()));
            sendUpdateLocalPosition();
        }
    }

    private static final String TAG = ShabService.class.getSimpleName();
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private final Object SYNC_START = new Object();

    private LocationManager locationManager;
    private LocationListener locationListener = new GpsLocationListener();
    private Location lastLocation;

    private SharedPreferences prefs;
    private ShabSocket shabSocket;
    private boolean alreadyStarted = false;

    private DataKeeper dataKeeper = new DataKeeper();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!alreadyStarted) {
            synchronized (SYNC_START) {
                if (!alreadyStarted) {
                    start();
                }
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
        shabSocket.send(ClientUpdate.serialize(dataKeeper.getClient()));
    }

    @Override
    public void newLine(String line) {
        Log.i(TAG, String.format("New line from socket: %s", line));
        Command command = Parser.parseRawCommand(line);
        switch (command) {
            case CLIENT_UPDATE:
                Client client = ClientUpdate.parse(line);
                sendUpdateClientPosition(client);
                break;
        }
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
        sendUpdateServiceStatus();
    }

    private void stop() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }

        if (shabSocket != null) {
            shabSocket.stop();
            shabSocket = null;
        }
        alreadyStarted = false;
        sendUpdateServiceStatus();
    }

    private void sendUpdateServiceStatus() {
        Intent intent = new Intent(I.UPDATE_SERVICE_STATUS);
        sendBroadcast(intent);
    }

    private void sendUpdateLocalPosition() {
        Intent intent = new Intent(I.UPDATE_LOCAL_POSITION);
        intent.putExtra("location", lastLocation);
        sendBroadcast(intent);
    }

    private void sendUpdateClientPosition(Client client) {
        Intent intent = new Intent(I.UPDATE_CLIENT_POSITION);
        intent.putExtra("client", client);
        sendBroadcast(intent);
    }
}
