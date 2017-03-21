package com.uncmorfi.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends SupportMapFragment {
    GoogleMap map;
    private static final LatLng CENTRAL = new LatLng(-31.439734, -64.189293);
    private static final LatLng BELGRANO = new LatLng(-31.416686, -64.189000);
    private static final LatLng CENTER = new LatLng(-31.428570, -64.184912);

    public MapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;


        map.addMarker(new MarkerOptions()
                .position(CENTRAL)
                .title("Sucursal Central"));


        map.addMarker(new MarkerOptions()
                .position(BELGRANO)
                .title("Sucursal Belgrano"));

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(CENTER)
                .zoom(10)
                .build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);

    }

}