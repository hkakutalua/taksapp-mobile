package com.taksapp.taksapp.ui.riders.taxirequests

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.riders.taxirequests.viewmodels.TaxiRequestViewModel
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.ui.utils.BitmapUtilities
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TaxiRequestActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val EXTRA_ERROR_KIND = "EXTRA_ERROR_KIND"
        const val ERROR_KIND_TAXI_REQUEST_TIMEOUT = "ERROR_KIND_TAXI_REQUEST_TIMEOUT"
        const val ERROR_KIND_TAXI_REQUEST_CANCELLED = "ERROR_KIND_TAXI_REQUEST_CANCELLED"
        const val EXTRA_TAXI_REQUEST = "EXTRA_TAXI_REQUEST"
    }

    private lateinit var taxiRequest: TaxiRequest
    private lateinit var taxiRequestViewModel: TaxiRequestViewModel
    private var googleMap: GoogleMap? = null
    private var driverLocationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taxi_request)

        blockBackNavigation()

        if (!intent.hasExtra(EXTRA_TAXI_REQUEST)) {
            finish()
            return
        }
        taxiRequest = intent.getSerializableExtra(EXTRA_TAXI_REQUEST) as TaxiRequest
        taxiRequestViewModel = getViewModel { parametersOf(taxiRequest) }

        observeNavigateBackEvent()
        observeTaxiRequestTimeoutEvent()
        observeCancelledMessageEvent()
        observeDriverArrivedEvent()
        observeTaxiRequestAcceptedEvent()
        observeDriverLocationEvents()

        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        if (map == null)
            return

        this.googleMap = map
    }

    private fun observeDriverLocationEvents() {
        taxiRequestViewModel.taxiRequestPresentation.observe(
            this, Observer { taxiRequestPresentation ->

            googleMap?.let { map ->
                if (taxiRequestPresentation.driverLocation == null)
                    return@Observer

                driverLocationMarker?.remove()

                val driverLocation = taxiRequestPresentation.driverLocation

                driverLocationMarker = map.addMarker(
                    MarkerOptions()
                        .title(taxiRequestPresentation.driverName)
                        .icon(
                            BitmapUtilities.getBitmapDescriptorForResource(
                                R.drawable.ic_driver_location_blue,
                                this
                            )
                        )
                        .position(LatLng(driverLocation.latitude, driverLocation.longitude))
                )
            }
        })

        taxiRequestViewModel.centerMapOnDriverEvent.observe(
            this, Observer { driverLocationEvent ->
                val driverLocation = driverLocationEvent.getContentIfNotHandled() ?: return@Observer
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(driverLocation.latitude, driverLocation.longitude), 15.0f))
            })
    }

    private fun observeNavigateBackEvent() {
        taxiRequestViewModel.navigateBackEvent.observe(
            this, Observer { finish() }
        )
    }

    private fun observeTaxiRequestTimeoutEvent() {
        taxiRequestViewModel.showTimeoutMessageAndNavigateBackEvent.observe(
            this, Observer {
                val intent = Intent()
                intent.putExtra(
                    EXTRA_ERROR_KIND,
                    ERROR_KIND_TAXI_REQUEST_TIMEOUT
                )
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        )
    }

    private fun observeTaxiRequestAcceptedEvent() {
        taxiRequestViewModel.navigateToAcceptedStateEvent.observe(
            this, Observer {
                navigateWithNoStackTo(R.id.driverArrivingFragment)
            })
    }

    private fun observeDriverArrivedEvent() {
        taxiRequestViewModel.navigateToDriverArrivedStateEvent.observe(
            this, Observer {
                navigateWithNoStackTo(R.id.driverArrivedFragment)
            })
    }

    private fun navigateWithNoStackTo(destination: Int) {
        val navController =
            Navigation.findNavController(this, R.id.fragment_navigation_host)

        navController.currentDestination?.let {
            navController.popBackStack(it.id, true)
        }

        navController.navigate(destination)
    }

    private fun observeCancelledMessageEvent() {
        taxiRequestViewModel.showCancelledMessageAndNavigateBackEvent.observe(
            this, Observer {
                val intent = Intent()
                intent.putExtra(
                    EXTRA_ERROR_KIND,
                    ERROR_KIND_TAXI_REQUEST_CANCELLED
                )
                setResult(Activity.RESULT_OK, intent)
                finish()
            })
    }

    private fun blockBackNavigation() {
        onBackPressedDispatcher.addCallback {}
    }
}
