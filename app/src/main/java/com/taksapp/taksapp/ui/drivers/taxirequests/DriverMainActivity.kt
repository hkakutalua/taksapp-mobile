package com.taksapp.taksapp.ui.drivers.taxirequests

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.drivers.taxirequests.viewmodels.DriverMainViewModel
import com.taksapp.taksapp.databinding.ActivityDriverMainBinding
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.time.ExperimentalTime

@ExperimentalTime
class DriverMainActivity : AppCompatActivity() {
    private val driverMainViewModel: DriverMainViewModel by viewModel()

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
