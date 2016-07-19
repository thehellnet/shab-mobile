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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.mobile.utility.MapClient;
import org.thehellnet.shab.protocol.bean.Client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sardylan on 17/07/16.
 */
public class MapFragment extends ShabFragment implements OnMapReadyCallback {

    private class UpdateLocalPositionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = (Location) intent.getExtras().get("location");
            if (location != null) {
                localPosition = new LatLng(location.getAltitude(), location.getLongitude());
                localMarker.setPosition(localPosition);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(localPosition));
            }
        }
    }

    private class UpdateClientPositionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Client client = (Client) intent.getExtras().get("client");
            if (client == null) {
                return;
            }

            for (int i = 0; i < mapClients.size(); i++) {
                if (mapClients.get(i).id.equals(client.getId())) {
                    MapClient mapClient = mapClients.get(i);
                    mapClient.name = client.getName();
                    mapClient.position = new LatLng(client.getPosition().getLatitude(), client.getPosition().getLongitude());
                    mapClient.marker.setPosition(mapClient.position);
                    mapClient.marker.setTitle(mapClient.name);
                    return;
                }
            }

            MapClient mapClient = new MapClient();
            mapClient.id = client.getId();
            mapClient.name = client.getName();
            mapClient.position = new LatLng(client.getPosition().getLatitude(), client.getPosition().getLongitude());
            mapClient.marker = googleMap.addMarker(new MarkerOptions().position(localPosition).title(mapClient.name));
            mapClients.add(mapClient);
        }
    }

    private UpdateLocalPositionReceiver updateLocalPositionReceiver;
    private UpdateClientPositionReceiver updateClientPositionReceiver;

    private GoogleMap googleMap;
    private LatLng localPosition;
    private Marker localMarker;

    private List<MapClient> mapClients = new ArrayList<>();

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
        if (updateLocalPositionReceiver != null) {
            getContext().unregisterReceiver(updateLocalPositionReceiver);
            updateLocalPositionReceiver = null;
        }
        if (updateClientPositionReceiver != null) {
            getContext().unregisterReceiver(updateClientPositionReceiver);
            updateClientPositionReceiver = null;
        }

        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMapObject) {
        googleMap = googleMapObject;

        double latitude = 0;
        double longitude = 0;

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            updateLocalPositionReceiver = new UpdateLocalPositionReceiver();
            getContext().registerReceiver(updateLocalPositionReceiver, new IntentFilter(I.UPDATE_LOCAL_POSITION));
        }

        localPosition = new LatLng(latitude, longitude);
        localMarker = googleMap.addMarker(
                new MarkerOptions().position(localPosition).title("Local"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(localPosition));

        updateClientPositionReceiver = new UpdateClientPositionReceiver();
        getContext().registerReceiver(updateClientPositionReceiver, new IntentFilter(I.UPDATE_CLIENT_POSITION));
    }
}
