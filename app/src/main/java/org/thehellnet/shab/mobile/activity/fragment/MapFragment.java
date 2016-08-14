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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.protocol.Position;

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

            localPosition = new LatLng(position.getLatitude(), position.getLongitude());
            localMarker.setPosition(localPosition);
            updateMapBoundsAndZoom();
        }
    }

    private class UpdateClientPositionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }

    private static final LatLng DEFAULT_POSITION = new LatLng(39.44510959999999, 9.540954499999998);

    private UpdateLocalPositionReceiver updateLocalPositionReceiver;
    private UpdateClientPositionReceiver updateClientPositionReceiver;

    private GoogleMap googleMap;

    private LatLng localPosition;
    private Marker localMarker;

    private boolean autoBoundAndZoom = true;

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

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            updateLocalPositionReceiver = new UpdateLocalPositionReceiver();
            getContext().registerReceiver(updateLocalPositionReceiver, new IntentFilter(I.UPDATE_LOCAL_POSITION));

            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                localPosition = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(localPosition);
                markerOptions.title("Local");
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_green));
                localMarker = googleMap.addMarker(markerOptions);
                updateMapBoundsAndZoom();
            }
        }

        updateClientPositionReceiver = new UpdateClientPositionReceiver();
        getContext().registerReceiver(updateClientPositionReceiver, new IntentFilter(I.UPDATE_CLIENT_POSITION));
    }

    private void updateMapBoundsAndZoom() {
        if (!autoBoundAndZoom) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean useDefault = true;

        if (localPosition != null) {
            useDefault = false;
            builder.include(localPosition);
        }

        if (useDefault) {
            builder.include(DEFAULT_POSITION);
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
    }
}
