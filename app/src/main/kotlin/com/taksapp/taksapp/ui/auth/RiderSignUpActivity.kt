package com.taksapp.taksapp.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.ActivityRiderSignUpBinding
import com.taksapp.taksapp.ui.auth.viewmodels.RiderSignUpViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class RiderSignUpActivity : AppCompatActivity() {
    private val riderSignUpViewModel: RiderSignUpViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityRiderSignUpBinding>(
            this, R.layout.activity_rider_sign_up)
        binding.viewModel = riderSignUpViewModel
        binding.lifecycleOwner = this

        binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        riderSignUpViewModel.navigateToOtpConfirmation.observe(this, Observer { event ->
            if (!event.hasBeenHandled) {
                val intent = Intent(this, RiderSignUpOtpConfirmationActivity::class.java)
                intent.putExtra(RiderSignUpOtpConfirmationActivity.EXTRA_OTP_ID, event.getContentIfNotHandled())
                startActivity(intent)
            }
        })

        riderSignUpViewModel.snackBarError.observe(this, Observer { event ->
            if (!event.hasBeenHandled) {
                Snackbar
                    .make(findViewById(android.R.id.content), event.peekContent().orEmpty(), Snackbar.LENGTH_LONG)
                    .show()
            }
        })
    }
}
