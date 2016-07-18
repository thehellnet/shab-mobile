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
                localPositionMarker.setPosition(localPosition);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(localPosition));
            }
        }
    }

    private UpdateLocalPositionReceiver updateLocalPositionReceiver;

    private GoogleMap googleMap;
    private LatLng localPosition;
    private Marker localPositionMarker;

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
        localPositionMarker = googleMap.addMarker(
                new MarkerOptions().position(localPosition).title("Local"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(localPosition));
    }
}
