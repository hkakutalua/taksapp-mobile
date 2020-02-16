package com.taksapp.taksapp.ui.drivers.taxirequests


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.drivers.taxirequests.viewmodels.DriverMainViewModel
import com.taksapp.taksapp.databinding.FragmentDriverMainBinding
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
class DriverMainFragment : Fragment() {
    companion object {
        private const val REQUEST_LOCATIONS_PERMISSION = 100
        private const val REQUEST_CHECK_LOCATIONS_SETTINGS = 200
        private const val REQUEST_CHANGE_LOCATION_SETTINGS = 300
    }

    private val driverMainViewModel by viewModel<DriverMainViewModel>()

    private var forcedLocationSettingsChangeDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentDriverMainBinding>(
            inflater, R.layout.fragment_driver_main, container, false)

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
        observeShowIncomingTaxiRequestEvent()

        askForLocationAccessPermission()

        return binding.root
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

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
            val locationSettingsClient = LocationServices.getSettingsClient(requireActivity())
            val locationSettingsCheckTask = locationSettingsClient.checkLocationSettings(builder.build())

            locationSettingsCheckTask.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_LOCATIONS_SETTINGS)
                }
            }
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            showForcedLocationSettingsChangeDialog()
        } else {
            askForLocationAccessPermission()
        }
    }

    private fun showForcedLocationSettingsChangeDialog() {
        forcedLocationSettingsChangeDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.text_location_permission)
            .setMessage(R.string.text_adjust_location_settings)
            .setCancelable(false)
            .setPositiveButton(R.string.text_go_to_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireActivity().packageName, null)
                startActivityForResult(intent, REQUEST_CHANGE_LOCATION_SETTINGS)
            }
            .create()

        forcedLocationSettingsChangeDialog?.show()
    }

    private fun askForLocationAccessPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATIONS_PERMISSION
            )
        }
    }

    private fun observeSnackBarErrorEvent() {
        driverMainViewModel.snackBarErrorEvent.observe(
            this, Observer { event ->
                if (!event.hasBeenHandled) {
                    val content = event.getContentIfNotHandled()
                    val rootView = requireActivity().findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, content ?: "", Snackbar.LENGTH_LONG).show()
                }
            })
    }

    private fun observeShowIncomingTaxiRequestEvent() {
        driverMainViewModel.showIncomingTaxiRequestEvent.observe(this, Observer { event ->
            if (event.hasBeenHandled)
                return@Observer

            event.getContentIfNotHandled()?.let {
                val navigateToIncomingTaxiRequestAction =
                    DriverMainFragmentDirections.toIncomingTaxiRequestAction(it)
                requireView().findNavController().navigate(navigateToIncomingTaxiRequestAction)
            }
        })
    }
}
