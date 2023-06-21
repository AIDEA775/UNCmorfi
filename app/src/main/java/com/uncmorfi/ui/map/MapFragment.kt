package com.uncmorfi.ui.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.uncmorfi.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.MapsInitializer
import kotlinx.android.synthetic.main.fragment_map.*

/**
 * Muestra las ubicaciones de los comedores en un GoogleMap.
 */
class MapFragment : Fragment() {
    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        mapView.onResume()

        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mapView.getMapAsync { onMapReady(it) }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_map)
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions()
                .position(CENTRAL)
                .title(getString(R.string.map_central)))

        mMap.addMarker(MarkerOptions()
                .position(BELGRANO)
                .title(getString(R.string.map_belgrano)))

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