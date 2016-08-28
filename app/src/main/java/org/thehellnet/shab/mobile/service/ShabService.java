package org.thehellnet.shab.mobile.service;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
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
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.joda.time.DateTime;
import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.activity.MainActivity;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.mobile.config.Prefs;
import org.thehellnet.shab.mobile.location.LocationListener;
import org.thehellnet.shab.mobile.protocol.ShabContext;
import org.thehellnet.shab.mobile.protocol.socket.ShabSocket;
import org.thehellnet.shab.mobile.protocol.socket.ShabSocketCallback;
import org.thehellnet.shab.mobile.utility.DeviceIdentifier;
import org.thehellnet.shab.protocol.entity.Client;
import org.thehellnet.shab.protocol.exception.AbstractProtocolException;
import org.thehellnet.shab.protocol.helper.Position;
import org.thehellnet.shab.protocol.line.ClientConnectLine;
import org.thehellnet.shab.protocol.line.ClientDisconnectLine;
import org.thehellnet.shab.protocol.line.ClientUpdateLine;
import org.thehellnet.shab.protocol.line.HabImageLine;
import org.thehellnet.shab.protocol.line.HabPositionLine;
import org.thehellnet.shab.protocol.line.HabTelemetryLine;
import org.thehellnet.shab.protocol.line.Line;
import org.thehellnet.shab.protocol.line.LineFactory;
import org.thehellnet.shab.protocol.line.ServerPingLine;

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
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    int usedSatellites = 0;
                    for (GpsSatellite gpsSatellite : gpsStatus.getSatellites()) {
                        if (gpsSatellite.usedInFix()) {
                            usedSatellites++;
                        }
                    }
                    useGpsInsteadNetwork = usedSatellites >= 3;
//                    Log.d(TAG, String.format("Used satellited: %d - useGpsInsteadNetwork: %s", usedSatellites, useGpsInsteadNetwork));
                    break;
            }
        }
    }

    private static final String TAG = ShabService.class.getSimpleName();
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final int POSITION_SEND_INTERVAL = 5;
    private static final int NOTIFICATION_ID = 1;

    private static final int SERVER_PING_DELAY = 2500;
    private static final int SERVER_PING_DELAY_MAX = 5000;

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

    private Thread serverPingThread;
    private DateTime lastServerPingDateTime;

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

        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setAction(Intent.ACTION_MAIN);
        activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("SHAB Service")
                .setContentText("SHAB is running")
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_notification)
                .build();

        startForeground(NOTIFICATION_ID, notification);

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
        line.setName(prefs.getString(Prefs.NAME, Prefs.Default.NAME));
        shabSocket.send(line);

        pingerStart();
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

        broadcastNewLine(line);

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
            } else if (line instanceof ServerPingLine) {
                doServerPing((ServerPingLine) line);
            }
        }
    }

    @Override
    public void disconnected() {
        broadcastSocketStatus(false);

        pingerStop();
    }

    @Override
    public void reconnecting() {
        Log.i(TAG, "Reconnecting");
    }

    private synchronized void start() {
        shabContext.clear();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, gpsLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, networkLocationListener);
            locationManager.addGpsStatusListener(gpsStatusListener);
        }

        shabSocket = new ShabSocket(this);
        shabSocket.start(prefs.getString(Prefs.SERVER_ADDRESS, Prefs.Default.SERVER_ADDRESS),
                prefs.getInt(Prefs.SOCKET_PORT, Prefs.Default.SOCKET_PORT));

        alreadyStarted = true;
        broadcastServiceStatus();
    }

    private synchronized void stop() {
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

    private void pingerStart() {
        serverPingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                lastServerPingDateTime = new DateTime();

                while (!serverPingThread.isInterrupted()) {
                    try {
                        Thread.sleep(SERVER_PING_DELAY);
                    } catch (InterruptedException ignored) {
                        break;
                    }

                    if (lastServerPingDateTime.plusSeconds(SERVER_PING_DELAY_MAX).isBeforeNow()) {
                        Log.w(TAG, "No answer to Ping, reconnecting...");
                        reconnect();
                        break;
                    }

                    ServerPingLine serverPingLine = new ServerPingLine();
                    serverPingLine.setTimestamp(DateTime.now().getMillis());
                    shabSocket.send(serverPingLine);
                    Log.d(TAG, "Sending ServerPing line");
                }
            }
        });
        serverPingThread.setDaemon(true);
        serverPingThread.start();
    }

    private void pingerStop() {
        if (serverPingThread != null) {
            serverPingThread.interrupt();
            try {
                serverPingThread.join();
            } catch (InterruptedException ignored) {
            }
            serverPingThread = null;
        }
    }

    private synchronized void reconnect() {
        stop();
        sleep(1500);
        start();
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

    private void broadcastNewLine(Line line) {
        Intent intent = new Intent(I.UPDATE_NEWLINE);
        intent.putExtra("line", line);
        sendBroadcast(intent);
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
        Position position = new Position(line.getLatitude(), line.getLongitude(), line.getAltitude());
        shabContext.getHab().setPosition(position);
        shabContext.getHab().setFixStatus(line.getFixStatus());

        Intent intent = new Intent(I.COMMAND_HAB_POSITION);
        sendBroadcast(intent);
    }

    private void doHabImage(HabImageLine line) {
        if (line.getSliceNum() == 1) {
            shabContext.getHab().clearImageData();
        }

        shabContext.getHab().setSliceTot(line.getSliceTot());
        shabContext.getHab().setSliceNum(line.getSliceNum());
        shabContext.getHab().appendImageData(line.getData());

        Intent intent = new Intent(I.COMMAND_HAB_IMAGE);
        sendBroadcast(intent);
    }

    private void doHabTelemetry(HabTelemetryLine line) {
        shabContext.getHab().setIntTemp(line.getIntTemp());
        shabContext.getHab().setExtTemp(line.getExtTemp());
        shabContext.getHab().setExtAlt(line.getExtAlt());

        Intent intent = new Intent(I.COMMAND_HAB_TELEMETRY);
        sendBroadcast(intent);
    }

    private void doServerPing(ServerPingLine line) {
        Log.d(TAG, "Receiving ServerPing line");
        lastServerPingDateTime = new DateTime();
    }

    private static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }
}
