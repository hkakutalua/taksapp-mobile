package com.taksapp.taksapp.ui.launch

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.taksapp.taksapp.application.launch.viewmodels.LaunchViewModel
import com.taksapp.taksapp.ui.riders.taxirequests.RiderMainActivity
import org.koin.android.viewmodel.ext.android.viewModel

class LaunchActivity : AppCompatActivity() {
    private val launchViewModel: LaunchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchViewModel.evaluateIfLoggedIn()

        launchViewModel.navigateToRiderMain.observe(this, Observer {
            startActivity(Intent(this, RiderMainActivity::class.java))
        })

        launchViewModel.navigateToDriverMain.observe(this, Observer {
            //startActivity(Intent(this, DriverMainActivity::class.java))
            Toast.makeText(this, "Not Implemented!", Toast.LENGTH_LONG).show()
        })

        launchViewModel.navigateToWelcome.observe(this, Observer {
            startActivity(Intent(this, WelcomeActivity::class.java))
        })
    }
}