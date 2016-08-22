package org.thehellnet.shab.mobile.activity.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.mobile.config.Prefs;
import org.thehellnet.shab.mobile.utility.ScreenConverter;
import org.thehellnet.shab.mobile.protocol.ShabContext;
import org.thehellnet.shab.protocol.entity.Client;
import org.thehellnet.shab.protocol.entity.Hab;
import org.thehellnet.shab.protocol.helper.Position;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sardylan on 17/07/16.
 */
public class MapFragment extends ShabFragment implements OnMapReadyCallback {

    private class UpdateLocalPositionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Position position = (Position) intent.getExtras().get("position");
            if (position == null) {
                return;
            }
            updateLocalMarker(position.getLatitude(), position.getLongitude());
        }
    }

    private class SocketStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getBooleanExtra("status", false)) {
                clearRemoteClients();
                removeLocalMarker();
                removeHabMarker();
            }
        }
    }

    private class CommandClientConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Client client = shabContext.findRemoteClientById(intent.getStringExtra("id"));
            if (client == null) {
                return;
            }
            addRemoteClient(client);
        }
    }

    private class CommandClientUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Client client = shabContext.findRemoteClientById(intent.getStringExtra("id"));
            if (client == null) {
                return;
            }
            updateRemoteClient(client);
        }
    }

    private class CommandClientDisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Client client = shabContext.findRemoteClientById(intent.getStringExtra("id"));
            if (client == null) {
                return;
            }
            removeRemoteClient(client);
        }
    }

    private class CommandHabPositionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Hab hab = shabContext.getHab();
            if (hab == null) {
                return;
            }
            updateHabMarker(hab.getPosition().getLatitude(), hab.getPosition().getLongitude());
        }
    }

    private class RemoteClient {

        public LatLng position;
        public Marker marker;
    }

    private static final String TAG = MapFragment.class.getSimpleName();
    private static final LatLng DEFAULT_POSITION = new LatLng(39.44510959999999, 9.540954499999998);

    private UpdateLocalPositionReceiver updateLocalPositionReceiver;
    private SocketStatusReceiver socketStatusReceiver;
    private CommandClientConnectReceiver commandClientConnectReceiver;
    private CommandClientUpdateReceiver commandClientUpdateReceiver;
    private CommandClientDisconnectReceiver commandClientDisconnectReceiver;
    private CommandHabPositionReceiver commandHabPositionReceiver;

    private GoogleMap googleMap;
    private ShabContext shabContext = ShabContext.getInstance();

    private LatLng localPosition;
    private Marker localMarker;

    private LatLng habPosition;
    private Marker habMarker;

    private Map<String, RemoteClient> remoteClients = new HashMap<>();

    private Switch autoBoundsSwitch;
    private Button mapTypeButton;

    @Override
    protected int getLayout() {
        return R.layout.fragment_map;
    }

    @Override
    public Fragments getBackFragment() {
        return Fragments.MAIN;
    }

    @Override
    public void onResume() {
        super.onResume();
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onPause() {
        stop();
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMapObject) {
        googleMap = googleMapObject;
        updateMapType();
        start();
    }

    @Override
    protected void initElements() {
        super.initElements();

        autoBoundsSwitch = (Switch) getActivity().findViewById(R.id.option_auto);
        autoBoundsSwitch.setChecked(prefs.getBoolean(Prefs.MAP_AUTOBOUNDS, Prefs.MAP_AUTOBOUNDS_DEFAULT));

        mapTypeButton = (Button) getActivity().findViewById(R.id.option_type);
        updateMapTypeButtonCaption();
    }

    @Override
    protected void setOnClickListeners() {
        super.setOnClickListeners();

        autoBoundsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefs.edit()
                        .putBoolean(Prefs.MAP_AUTOBOUNDS, autoBoundsSwitch.isChecked())
                        .apply();
                updateMapBounds();
            }
        });

        mapTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mapType = prefs.getInt(Prefs.MAP_TYPE, Prefs.MAP_TYPE_DEFAULT);

                if (mapTypeButton.getText().equals(getString(R.string.layout_map_type_normal))) {
                    mapType = GoogleMap.MAP_TYPE_NORMAL;
                }

                if (mapTypeButton.getText().equals(getString(R.string.layout_map_type_satellite))) {
                    mapType = GoogleMap.MAP_TYPE_SATELLITE;
                }

                prefs.edit()
                        .putInt(Prefs.MAP_TYPE, mapType)
                        .apply();

                updateMapTypeButtonCaption();
                updateMapType();
            }
        });
    }

    private void start() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocalPositionReceiver = new UpdateLocalPositionReceiver();
            getContext().registerReceiver(updateLocalPositionReceiver, new IntentFilter(I.UPDATE_LOCAL_POSITION));

            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                updateLocalMarker(location.getLatitude(), location.getLongitude());
            }
        }

        initMapFromShabContext();

        socketStatusReceiver = new SocketStatusReceiver();
        getContext().registerReceiver(socketStatusReceiver, new IntentFilter(I.UPDATE_SOCKET_STATUS));

        commandClientConnectReceiver = new CommandClientConnectReceiver();
        getContext().registerReceiver(commandClientConnectReceiver, new IntentFilter(I.COMMAND_CLIENT_CONNECT));

        commandClientUpdateReceiver = new CommandClientUpdateReceiver();
        getContext().registerReceiver(commandClientUpdateReceiver, new IntentFilter(I.COMMAND_CLIENT_UPDATE));

        commandClientDisconnectReceiver = new CommandClientDisconnectReceiver();
        getContext().registerReceiver(commandClientDisconnectReceiver, new IntentFilter(I.COMMAND_CLIENT_DISCONNECT));

        commandHabPositionReceiver = new CommandHabPositionReceiver();
        getContext().registerReceiver(commandHabPositionReceiver, new IntentFilter(I.COMMAND_HAB_POSITION));
    }

    private void stop() {
        if (updateLocalPositionReceiver != null) {
            getContext().unregisterReceiver(updateLocalPositionReceiver);
            updateLocalPositionReceiver = null;
        }

        if (socketStatusReceiver != null) {
            getContext().unregisterReceiver(socketStatusReceiver);
            socketStatusReceiver = null;
        }

        if (commandClientConnectReceiver != null) {
            getContext().unregisterReceiver(commandClientConnectReceiver);
            commandClientConnectReceiver = null;
        }

        if (commandClientUpdateReceiver != null) {
            getContext().unregisterReceiver(commandClientUpdateReceiver);
            commandClientUpdateReceiver = null;
        }

        if (commandClientDisconnectReceiver != null) {
            getContext().unregisterReceiver(commandClientDisconnectReceiver);
            commandClientDisconnectReceiver = null;
        }

        if (commandHabPositionReceiver != null) {
            getContext().unregisterReceiver(commandHabPositionReceiver);
            commandHabPositionReceiver = null;
        }
    }

    private void updateMapTypeButtonCaption() {
        switch (prefs.getInt(Prefs.MAP_TYPE, Prefs.MAP_TYPE_DEFAULT)) {
            case GoogleMap.MAP_TYPE_NORMAL:
                mapTypeButton.setText(R.string.layout_map_type_satellite);
                break;
            case GoogleMap.MAP_TYPE_SATELLITE:
                mapTypeButton.setText(R.string.layout_map_type_normal);
                break;
        }
    }

    private void updateMapType() {
        googleMap.setMapType(prefs.getInt(Prefs.MAP_TYPE, Prefs.MAP_TYPE_DEFAULT));
    }

    private void updateMapBounds() {
        if (!prefs.getBoolean(Prefs.MAP_AUTOBOUNDS, Prefs.MAP_AUTOBOUNDS_DEFAULT)) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean useDefault = true;

        if (habPosition != null) {
            useDefault = false;
            builder.include(habPosition);
        }

        if (localPosition != null) {
            useDefault = false;
            builder.include(localPosition);
        }

        for (RemoteClient remoteClient : remoteClients.values()) {
            if (remoteClient.position == null) {
                continue;
            }
            builder.include(remoteClient.position);
            useDefault = false;
        }

        if (useDefault) {
            builder.include(DEFAULT_POSITION);
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), ScreenConverter.dpToPixel(30)));
    }

    private void initMapFromShabContext() {
        clearRemoteClients();
        for (Client client : shabContext.getRemoteClients()) {
            addRemoteClient(client);
        }
        updateMapBounds();
    }

    private void clearRemoteClients() {
        for (RemoteClient remoteClient : remoteClients.values()) {
            remoteClient.marker.remove();
            remoteClient.marker = null;
        }
        remoteClients.clear();
    }

    private void addRemoteClient(Client client) {
        RemoteClient remoteClient = new RemoteClient();
        Position position = client.getPosition();
        if (position != null) {
            remoteClient.position = new LatLng(position.getLatitude(), position.getLongitude());
            remoteClient.marker = googleMap.addMarker(new MarkerOptions()
                    .position(remoteClient.position)
                    .title(client.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_yellow)));
        }
        remoteClients.put(client.getId(), remoteClient);
        updateMapBounds();
        Log.i(TAG, String.format("addRemoteClient %s", client));
    }

    private void updateRemoteClient(Client client) {
        Position position = client.getPosition();
        if (position == null || !remoteClients.containsKey(client.getId())) {
            return;
        }
        RemoteClient remoteClient = remoteClients.get(client.getId());
        remoteClient.position = new LatLng(client.getPosition().getLatitude(), client.getPosition().getLongitude());
        if (remoteClient.marker == null) {
            remoteClient.marker = googleMap.addMarker(new MarkerOptions()
                    .position(remoteClient.position)
                    .title(client.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_yellow)));
        } else {
            remoteClient.marker.setPosition(remoteClient.position);
        }
        remoteClients.put(client.getId(), remoteClient);
        updateMapBounds();
        Log.i(TAG, String.format("updateRemoteClient %s", client));
    }

    private void removeRemoteClient(Client client) {
        if (!remoteClients.containsKey(client.getId())) {
            return;
        }
        RemoteClient remoteClient = remoteClients.get(client.getId());
        if (remoteClient.marker != null) {
            remoteClient.marker.remove();
            remoteClient.marker = null;
        }
        remoteClients.remove(client.getId());
        Log.i(TAG, String.format("removeRemoteClient %s", client));
    }

    private void updateLocalMarker(double latitude, double longitude) {
        localPosition = new LatLng(latitude, longitude);

        if (localMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(localPosition);
            markerOptions.title("Local");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_green));
            localMarker = googleMap.addMarker(markerOptions);
        } else {
            localMarker.setPosition(localPosition);
        }

        updateMapBounds();
    }

    private void removeLocalMarker() {
        if (localMarker != null) {
            localMarker.remove();
            localMarker = null;
        }
        updateMapBounds();
    }

    private void updateHabMarker(double latitude, double longitude) {
        habPosition = new LatLng(latitude, longitude);

        if (habMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(habPosition);
            markerOptions.title("HAB");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_red));
            habMarker = googleMap.addMarker(markerOptions);
        } else {
            habMarker.setPosition(habPosition);
        }

        updateMapBounds();
    }

    private void removeHabMarker() {
        if (habMarker != null) {
            habMarker.remove();
            habMarker = null;
        }
        updateMapBounds();
    }
}
