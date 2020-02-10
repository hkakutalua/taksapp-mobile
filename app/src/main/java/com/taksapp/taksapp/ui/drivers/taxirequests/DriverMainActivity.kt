package com.taksapp.taksapp.ui.drivers.taxirequests

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.drivers.taxirequests.viewmodels.DriverMainViewModel
import com.taksapp.taksapp.databinding.ActivityDriverMainBinding
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
class DriverMainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_LOCATIONS_PERMISSION = 100
        private const val REQUEST_CHECK_LOCATIONS_SETTINGS = 200
        private const val REQUEST_CHANGE_LOCATION_SETTINGS = 300
    }

    private val driverMainViewModel: DriverMainViewModel by viewModel()
    private var forcedLocationSettingsChangeDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil
            .setContentView<ActivityDriverMainBinding>(this, R.layout.activity_driver_main)
        binding.viewModel = driverMainViewModel
        binding.lifecycleOwner = this

        binding.switchOnline.setOnCheckedChangeListener { toggle, isChecked ->
            if (!toggle.isPressed)
                return@setOnCheckedChangeListener

            if (isChecked) {
                driverMainViewModel.switchToOnline()
            } else {
                driverMainViewModel.switchToOffline()
            }
        }

        observeSnackBarErrorEvent()
        askForLocationAccessPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK &&
            (requestCode == REQUEST_CHECK_LOCATIONS_SETTINGS || requestCode == REQUEST_CHANGE_LOCATION_SETTINGS)) {
            askForLocationAccessPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != REQUEST_LOCATIONS_PERMISSION || grantResults.isEmpty())
            return

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create()?.apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }!!

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            val locationSettingsClient = LocationServices.getSettingsClient(this@DriverMainActivity)
            val locationSettingsCheckTask = locationSettingsClient.checkLocationSettings(builder.build())

            locationSettingsCheckTask.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    exception.startResolutionForResult(this@DriverMainActivity, REQUEST_CHECK_LOCATIONS_SETTINGS)
                }
            }
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            showForcedLocationSettingsChangeDialog()
        } else {
            askForLocationAccessPermission()
        }
    }

    private fun showForcedLocationSettingsChangeDialog() {
        forcedLocationSettingsChangeDialog = AlertDialog.Builder(this)
            .setTitle(R.string.text_location_permission)
            .setMessage(R.string.text_adjust_location_settings)
            .setCancelable(false)
            .setPositiveButton(R.string.text_go_to_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivityForResult(intent, REQUEST_CHANGE_LOCATION_SETTINGS)
            }
            .create()

        forcedLocationSettingsChangeDialog?.show()
    }

    private fun askForLocationAccessPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATIONS_PERMISSION)
        }
    }

    private fun observeSnackBarErrorEvent() {
        driverMainViewModel.snackBarErrorEvent.observe(
            this, Observer { event ->
                if (!event.hasBeenHandled) {
                    val content = event.getContentIfNotHandled()
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, content ?: "", Snackbar.LENGTH_LONG).show()
                }
            })
    }
}
