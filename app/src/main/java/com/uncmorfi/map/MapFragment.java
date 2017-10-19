package com.uncmorfi.map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uncmorfi.R;

/**
 * Muestra las ubicaciones de los comedores en un GoogleMap.
 */
public class MapFragment extends SupportMapFragment {
    private static final LatLng CENTRAL = new LatLng(-31.439734, -64.189293);
    private static final LatLng BELGRANO = new LatLng(-31.416686, -64.189000);
    private static final LatLng CENTER = new LatLng(-31.428570, -64.184912);
    GoogleMap mMap;

    public MapFragment() {}

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_map);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions()
                .position(CENTRAL)
                .title("Sucursal Central"));

        mMap.addMarker(new MarkerOptions()
                .position(BELGRANO)
                .title("Sucursal Belgrano"));

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(CENTER)
                .zoom(14)
                .build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}