package com.taksapp.taksapp.ui.taxi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.taksapp.taksapp.R
import com.taksapp.taksapp.ui.taxi.presentationmodels.FareEstimationPresentationModel
import com.taksapp.taksapp.ui.taxi.presentationmodels.LocationPresentationModel
import com.taksapp.taksapp.ui.taxi.viewmodels.FareEstimationViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class RiderMainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val fareEstimationViewModel: FareEstimationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider_main)
        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        Navigation.findNavController(this, R.id.fragment_navigation_host)
    }

    override fun onMapReady(map: GoogleMap?) {
        fareEstimationViewModel.fareEstimationWithRoute.observe(this, Observer { fareEstimation ->
            drawDirections(map, fareEstimation)
            Navigation.findNavController(this, R.id.fragment_navigation_host)
                .navigate(R.id.action_locationsSelectionFragment_to_faresEstimatesFragment)
        })

        fareEstimationViewModel.clearDirectionsEvent.observe(this, Observer {
            map?.clear()
        })
    }

    private fun drawDirections(
        map: GoogleMap?,
        fareEstimation: FareEstimationPresentationModel
    ) {
        map?.clear()

        val stepsPairs: List<Pair<LocationPresentationModel, LocationPresentationModel>> =
            fareEstimation.steps

        val northEast = fareEstimation.northEastBound
        val southWest = fareEstimation.southWestBound

        val cameraBounds = LatLngBounds.builder()
            .include(LatLng(northEast.latitude, northEast.longitude))
            .include(LatLng(southWest.latitude, southWest.longitude))
            .build()

        map?.animateCamera(CameraUpdateFactory.newLatLngBounds(cameraBounds, 10))

        val encodedSteps = stepsPairs.flatMap { pair ->
            val firstLocation = LatLng(pair.first.latitude, pair.first.longitude)
            val secondLocation = LatLng(pair.second.latitude, pair.second.longitude)
            listOf(firstLocation, secondLocation)
        }

        map?.addPolyline(
            PolylineOptions()
                .addAll(encodedSteps)
                .color(0xFFC92F00.toInt())
        )
    }
}
