package com.taksapp.taksapp.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.auth.viewmodels.LoginViewModel
import com.taksapp.taksapp.databinding.ActivityDriverLoginBinding
import com.taksapp.taksapp.ui.drivers.taxirequests.DriverMainActivity
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.time.ExperimentalTime

class DriverLoginActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModel()

    @ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding : ActivityDriverLoginBinding = DataBindingUtil
            .setContentView(this, R.layout.activity_driver_login)
        binding.viewModel = loginViewModel
        binding.lifecycleOwner = this

        binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loginViewModel.snackBarError.observe(this, Observer { value ->
            Snackbar
                .make(findViewById(android.R.id.content), value, Snackbar.LENGTH_LONG)
                .show()
        })

        loginViewModel.navigateToMain.observe(this, Observer {
            val intent = Intent(this, DriverMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        })
    }
}
