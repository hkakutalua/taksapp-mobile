package com.taksapp.taksapp.ui.auth

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.ActivityRiderSignUpOtpConfirmationBinding
import com.taksapp.taksapp.ui.auth.viewmodels.RiderSignUpOtpConfirmationViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class RiderSignUpOtpConfirmationActivity : AppCompatActivity() {
    companion object {
        val TAG = RiderSignUpOtpConfirmationActivity::class.simpleName
        const val EXTRA_OTP_ID = "OTP_ID"
    }

    private val otpConfirmationViewModel: RiderSignUpOtpConfirmationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityRiderSignUpOtpConfirmationBinding>(
            this, R.layout.activity_rider_sign_up_otp_confirmation)
        binding.viewModel = otpConfirmationViewModel
        binding.lifecycleOwner = this

        if (!intent.hasExtra(EXTRA_OTP_ID)) {
            Log.e(TAG, "Otp id was not passed southWest this activity. Navigating back...")
            finish()
        } else {
            binding.lifecycleOwner = this

            binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            otpConfirmationViewModel.snackBarMessage.observe(this, Observer { event ->
                if (!event.hasBeenHandled) {
                    Snackbar
                        .make(findViewById(android.R.id.content), event.peekContent().orEmpty(), Snackbar.LENGTH_LONG)
                        .show()
                }
            })

            otpConfirmationViewModel.navigateToRiderLogin.observe(this, Observer { event ->
                if (!event.hasBeenHandled) {
                    startActivity(Intent(this, RiderLoginActivity::class.java))
                    finish()
                }
            })
        }
    }

    fun confirmSignUpOtp(view: View) {
        otpConfirmationViewModel.confirmSignUpWithOtp(intent.getStringExtra(EXTRA_OTP_ID))
    }
}
