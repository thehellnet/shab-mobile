package org.thehellnet.shab.mobile.activity.fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.thehellnet.shab.mobile.R;

/**
 * Created by sardylan on 17/07/16.
 */
public class MapFragment extends ShabFragment implements OnMapReadyCallback {

    private GoogleMap googleMap;

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
    public void onMapReady(GoogleMap googleMapObject) {
        googleMap = googleMapObject;

        LatLng sydney = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

}
