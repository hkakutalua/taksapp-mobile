package com.taksapp.taksapp.ui.taxi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.taksapp.taksapp.R

class RiderMainActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider_main)
        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        Navigation.findNavController(this, R.id.fragment_navigation_host)
    }

    override fun onMapReady(map: GoogleMap?) {

    }
}
