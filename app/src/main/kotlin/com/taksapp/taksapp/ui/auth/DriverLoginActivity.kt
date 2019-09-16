package com.taksapp.taksapp.ui.auth

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.ActivityDriverLoginBinding

class DriverLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding : ActivityDriverLoginBinding = DataBindingUtil
                .setContentView(this, R.layout.activity_driver_login)

        binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
