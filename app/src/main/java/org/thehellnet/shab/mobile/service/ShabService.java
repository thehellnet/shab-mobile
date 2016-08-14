package org.thehellnet.shab.mobile.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.joda.time.DateTime;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.mobile.config.Prefs;
import org.thehellnet.shab.mobile.location.LocationListener;
import org.thehellnet.shab.mobile.utility.DeviceIdentifier;
import org.thehellnet.shab.protocol.Position;
import org.thehellnet.shab.protocol.ShabContext;
import org.thehellnet.shab.protocol.line.ClientConnectLine;
import org.thehellnet.shab.protocol.line.ClientUpdateLine;
import org.thehellnet.shab.protocol.socket.ShabSocket;
import org.thehellnet.shab.protocol.socket.ShabSocketCallback;

/**
 * Created by sardylan on 16/07/16.
 */
public class ShabService extends Service implements ShabSocketCallback {

    private class NetworkLocationListener extends LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (!useGpsInsteadNetwork) {
                newLocation(location);
            }
        }
    }

    private class GpsLocationListener extends LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            newLocation(location);
        }
    }

    private class GpsStatusListener implements GpsStatus.Listener {

        @Override
        public void onGpsStatusChanged(int status) {
            switch (status) {
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.d(TAG, "onGpsStatusChanged: GPS_EVENT_STARTED");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.d(TAG, "onGpsStatusChanged: GPS_EVENT_STOPPED");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.d(TAG, "onGpsStatusChanged: GPS_EVENT_FIRST_FIX");
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.d(TAG, "onGpsStatusChanged: GPS_EVENT_SATELLITE_STATUS");
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);

                    int usedSatellites = 0;
                    for (GpsSatellite gpsSatellite : gpsStatus.getSatellites()) {
                        if (gpsSatellite.usedInFix()) {
                            usedSatellites++;
                        }
                    }

                    Log.d(TAG, String.format("Used satellited: %d", usedSatellites));

                    useGpsInsteadNetwork = usedSatellites >= 3;
                    break;
            }
        }
    }

    private static final String TAG = ShabService.class.getSimpleName();
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private final Object SYNC_START = new Object();

    private LocationManager locationManager;
    private LocationListener gpsLocationListener = new GpsLocationListener();
    private LocationListener networkLocationListener = new NetworkLocationListener();
    private GpsStatusListener gpsStatusListener = new GpsStatusListener();
    private boolean useGpsInsteadNetwork = false;

    private SharedPreferences prefs;
    private ShabSocket shabSocket;
    private boolean alreadyStarted = false;

    private ShabContext shabContext = new ShabContext();
    private DateTime lastLocalPositionSend = new DateTime();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

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
        ClientConnectLine line = new ClientConnectLine();
        line.setId(DeviceIdentifier.getDeviceId());
        line.setName(prefs.getString(Prefs.NAME, Prefs.NAME_DEFAULT));
        shabSocket.send(line);

        sendLocalPosition();
    }

    @Override
    public void newLine(String line) {
        Log.i(TAG, String.format("New line from socket: %s", line));
//        Command command = Parser.parseRawCommand(line);
//        switch (command) {
//            case CLIENT_UPDATE:
//                Client client = ClientUpdate.parse(line);
//                sendUpdateClientPosition(client);
//                break;
//        }
    }

    @Override
    public void disconnected() {
        stop();
    }

    private void start() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, gpsLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, networkLocationListener);
            locationManager.addGpsStatusListener(gpsStatusListener);
        }

        shabSocket = new ShabSocket(this);
        shabSocket.start(prefs.getString(Prefs.SERVER_ADDRESS, Prefs.SERVER_ADDRESS_DEFAULT),
                prefs.getInt(Prefs.SOCKET_PORT, Prefs.SOCKET_PORT_DEFAULT));

        alreadyStarted = true;
        broadcastServiceStatus();
    }

    private void stop() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(networkLocationListener);
            locationManager.removeUpdates(gpsLocationListener);
            locationManager.removeGpsStatusListener(gpsStatusListener);
        }

        if (shabSocket != null) {
            shabSocket.stop();
            shabSocket = null;
        }

        alreadyStarted = false;
        broadcastServiceStatus();
    }

    private void newLocation(Location location) {
        Position localClientPosition = new Position();
        localClientPosition.setLatitude(location.getLatitude());
        localClientPosition.setLongitude(location.getLongitude());
        localClientPosition.setAltitude(location.getAltitude());
        shabContext.getLocalClient().setPosition(localClientPosition);

        broadcastLocalPosition();

        if (lastLocalPositionSend.plusSeconds(5).isBeforeNow()) {
            sendLocalPosition();
            lastLocalPositionSend = new DateTime();
        }
    }

    private void sendLocalPosition() {
        if (shabContext.getLocalClient().getPosition() == null) {
            return;
        }

        ClientUpdateLine line = new ClientUpdateLine();
        line.setId(DeviceIdentifier.getDeviceId());
        line.setLatitude(shabContext.getLocalClient().getPosition().getLatitude());
        line.setLongitude(shabContext.getLocalClient().getPosition().getLongitude());
        line.setAltitude(shabContext.getLocalClient().getPosition().getAltitude());
        shabSocket.send(line);
    }

    private void broadcastServiceStatus() {
        Intent intent = new Intent(I.UPDATE_SERVICE_STATUS);
        sendBroadcast(intent);
    }

    private void broadcastLocalPosition() {
        Intent intent = new Intent(I.UPDATE_LOCAL_POSITION);
        intent.putExtra("position", shabContext.getLocalClient().getPosition());
        sendBroadcast(intent);
    }
}
