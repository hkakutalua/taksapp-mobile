package com.taksapp.taksapp.ui.auth

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.ActivityRiderSignUpOtpConfirmationBinding

class RiderSignUpOtpConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityRiderSignUpOtpConfirmationBinding>(
            this, R.layout.activity_rider_sign_up_otp_confirmation)
        binding.lifecycleOwner = this

        binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
