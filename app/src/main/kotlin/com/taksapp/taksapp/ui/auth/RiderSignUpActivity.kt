package com.taksapp.taksapp.ui.auth

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
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
    }
}
