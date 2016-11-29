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

    public MapFragment() {
        // Required empty public constructor
    }

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

        LatLng central = new LatLng(-31.439734, -64.189293);
        map.addMarker(new MarkerOptions()
                .position(central)
                .title("Sucursal Central"));

        LatLng belgrano = new LatLng(-31.416686, -64.189000);
        map.addMarker(new MarkerOptions()
                .position(belgrano)
                .title("Sucursal Belgrano"));

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(central)
                .zoom(16)
                .build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
