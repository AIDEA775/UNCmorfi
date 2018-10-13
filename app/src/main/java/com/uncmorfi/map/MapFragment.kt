package com.uncmorfi.map

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.uncmorfi.R

/**
 * Muestra las ubicaciones de los comedores en un GoogleMap.
 */
class MapFragment : SupportMapFragment() {
    private lateinit var mMap: GoogleMap

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_map)
    }

    fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions()
                .position(CENTRAL)
                .title("Sucursal Central"))

        mMap.addMarker(MarkerOptions()
                .position(BELGRANO)
                .title("Sucursal Belgrano"))

        val cameraPosition = CameraPosition.builder()
                .target(CENTER)
                .zoom(14f)
                .build()

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    companion object {
        private val CENTRAL = LatLng(-31.439734, -64.189293)
        private val BELGRANO = LatLng(-31.416686, -64.189000)
        private val CENTER = LatLng(-31.428570, -64.184912)
    }
}