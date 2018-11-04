package com.uncmorfi.map

import android.os.Bundle
import android.support.v4.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.uncmorfi.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View


/**
 * Muestra las ubicaciones de los comedores en un GoogleMap.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity != null) {
            val mapFragment = activity!!.supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_map)
    }

    override fun onMapReady(googleMap: GoogleMap) {
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