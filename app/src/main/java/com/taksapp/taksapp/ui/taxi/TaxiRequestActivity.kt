package com.taksapp.taksapp.ui.taxi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.taxirequest.viewmodels.TaxiRequestViewModel
import com.taksapp.taksapp.domain.TaxiRequest
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class TaxiRequestActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val EXTRA_ERROR_KIND = "EXTRA_ERROR_KIND"
        const val ERROR_KIND_TAXI_REQUEST_TIMEOUT = "ERROR_KIND_TAXI_REQUEST_TIMEOUT"
        const val ERROR_KIND_TAXI_REQUEST_CANCELLED = "ERROR_KIND_TAXI_REQUEST_CANCELLED"
        const val EXTRA_TAXI_REQUEST = "EXTRA_TAXI_REQUEST"
    }

    private lateinit var taxiRequest: TaxiRequest
    private lateinit var taxiRequestViewModel: TaxiRequestViewModel

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

        observeCancelledMessageEvent()
        observeDriverArrivedEvent()
        observeTaxiRequestAcceptedEvent()

        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        if (map == null)
            return
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
