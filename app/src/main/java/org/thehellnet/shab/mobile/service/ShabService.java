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
import org.thehellnet.shab.protocol.LineFactory;
import org.thehellnet.shab.protocol.Position;
import org.thehellnet.shab.protocol.ShabContext;
import org.thehellnet.shab.protocol.entity.Client;
import org.thehellnet.shab.protocol.exception.AbstractProtocolException;
import org.thehellnet.shab.protocol.line.ClientConnectLine;
import org.thehellnet.shab.protocol.line.ClientDisconnectLine;
import org.thehellnet.shab.protocol.line.ClientUpdateLine;
import org.thehellnet.shab.protocol.line.HabImageLine;
import org.thehellnet.shab.protocol.line.HabPositionLine;
import org.thehellnet.shab.protocol.line.HabTelemetryLine;
import org.thehellnet.shab.protocol.line.Line;
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
                handleNewLocationFromProviders(location);
            }
        }
    }

    private class GpsLocationListener extends LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            handleNewLocationFromProviders(location);
        }
    }

    private class GpsStatusListener implements GpsStatus.Listener {

        @Override
        public void onGpsStatusChanged(int status) {
            switch (status) {
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    int usedSatellites = 0;
                    for (GpsSatellite gpsSatellite : gpsStatus.getSatellites()) {
                        if (gpsSatellite.usedInFix()) {
                            usedSatellites++;
                        }
                    }
                    useGpsInsteadNetwork = usedSatellites >= 3;
                    Log.d(TAG, String.format("Used satellited: %d - useGpsInsteadNetwork: %s", usedSatellites, useGpsInsteadNetwork));
                    break;
            }
        }
    }

    private static final String TAG = ShabService.class.getSimpleName();
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final int POSITION_SEND_INTERVAL = 5;

    private final Object SYNC_START = new Object();
    private final Object SYNC_LINEPARSE = new Object();

    private LocationManager locationManager;
    private LocationListener gpsLocationListener = new GpsLocationListener();
    private LocationListener networkLocationListener = new NetworkLocationListener();
    private GpsStatusListener gpsStatusListener = new GpsStatusListener();
    private boolean useGpsInsteadNetwork = false;

    private SharedPreferences prefs;
    private ShabSocket shabSocket;
    private boolean alreadyStarted = false;

    private ShabContext shabContext = ShabContext.getInstance();
    private DateTime lastLocalPositionSend;

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
        broadcastSocketStatus(true);

        ClientConnectLine line = new ClientConnectLine();
        line.setId(DeviceIdentifier.getDeviceId());
        line.setName(prefs.getString(Prefs.NAME, Prefs.NAME_DEFAULT));
        shabSocket.send(line);

        sendLocalPosition();
    }

    @Override
    public void newLine(String rawLine) {
        Log.d(TAG, String.format("New line from socket: %s", rawLine));

        Line line;
        try {
            line = LineFactory.parse(rawLine);
        } catch (AbstractProtocolException e) {
            e.printStackTrace();
            return;
        }

        synchronized (SYNC_LINEPARSE) {
            if (line instanceof ClientConnectLine) {
                doClientConnected((ClientConnectLine) line);
            } else if (line instanceof ClientUpdateLine) {
                doClientUpdate((ClientUpdateLine) line);
            } else if (line instanceof ClientDisconnectLine) {
                doClientDisconnected((ClientDisconnectLine) line);
            } else if (line instanceof HabPositionLine) {
                doHabPosition((HabPositionLine) line);
            } else if (line instanceof HabImageLine) {
                doHabImage((HabImageLine) line);
            } else if (line instanceof HabTelemetryLine) {
                doHabTelemetry((HabTelemetryLine) line);
            }
        }
    }

    @Override
    public void disconnected() {
        broadcastSocketStatus(false);
        stop();
    }

    private void start() {
        shabContext.clear();

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

        shabContext.clear();
        lastLocalPositionSend = null;

        alreadyStarted = false;
        broadcastServiceStatus();
    }

    private void handleNewLocationFromProviders(Location location) {
        Position localClientPosition = new Position();
        localClientPosition.setLatitude(location.getLatitude());
        localClientPosition.setLongitude(location.getLongitude());
        localClientPosition.setAltitude(location.getAltitude());
        shabContext.getLocalClient().setPosition(localClientPosition);

        broadcastLocalPosition();

        if (lastLocalPositionSend == null
                || lastLocalPositionSend.plusSeconds(POSITION_SEND_INTERVAL).isBeforeNow()) {
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

    private void broadcastSocketStatus(boolean status) {
        Intent intent = new Intent(I.UPDATE_SOCKET_STATUS);
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    private void broadcastLocalPosition() {
        Intent intent = new Intent(I.UPDATE_LOCAL_POSITION);
        intent.putExtra("position", shabContext.getLocalClient().getPosition());
        sendBroadcast(intent);
    }

    private void doClientConnected(ClientConnectLine line) {
        Client client = new Client();
        client.setId(line.getId());
        client.setName(line.getName());
        shabContext.getRemoteClients().add(client);

        Intent intent = new Intent(I.COMMAND_CLIENT_CONNECT);
        intent.putExtra("id", client.getId());
        sendBroadcast(intent);
    }

    private void doClientUpdate(ClientUpdateLine line) {
        Log.i(TAG, String.format("Client %s Update", line.getId()));

        Client client = shabContext.findRemoteClientById(line.getId());
        if (client == null) {
            return;
        }
        int index = shabContext.getRemoteClients().lastIndexOf(client);

        Position position = new Position(line.getLatitude(), line.getLongitude(), line.getAltitude());
        client.setPosition(position);

        shabContext.getRemoteClients().set(index, client);

        Intent intent = new Intent(I.COMMAND_CLIENT_UPDATE);
        intent.putExtra("id", client.getId());
        sendBroadcast(intent);
    }

    private void doClientDisconnected(ClientDisconnectLine line) {
        Client client = shabContext.findRemoteClientById(line.getId());
        if (client == null) {
            return;
        }
        shabContext.getRemoteClients().remove(client);

        Intent intent = new Intent(I.COMMAND_CLIENT_DISCONNECT);
        intent.putExtra("id", client.getId());
        sendBroadcast(intent);
    }

    private void doHabPosition(HabPositionLine line) {
        Intent intent = new Intent(I.COMMAND_HAB_POSITION);
        sendBroadcast(intent);
    }

    private void doHabImage(HabImageLine line) {
        Intent intent = new Intent(I.COMMAND_HAB_IMAGE);
        sendBroadcast(intent);
    }

    private void doHabTelemetry(HabTelemetryLine line) {
        Intent intent = new Intent(I.COMMAND_HAB_TELEMETRY);
        sendBroadcast(intent);
    }
}
