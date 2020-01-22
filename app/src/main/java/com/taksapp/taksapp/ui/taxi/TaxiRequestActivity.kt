package com.taksapp.taksapp.ui.taxi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.addCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.taksapp.taksapp.R
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.application.taxirequest.viewmodels.TaxiRequestViewModel
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class TaxiRequestActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        const val EXTRA_ERROR_KIND = "EXTRA_ERROR_KIND"
        const val ERROR_KIND_TAXI_REQUEST_TIMEOUT = "ERROR_KIND_TAXI_REQUEST_TIMEOUT"
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

        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {}

    private fun blockBackNavigation() {
        onBackPressedDispatcher.addCallback {}
    }
}
