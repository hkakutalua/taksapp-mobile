package com.taksapp.taksapp.ui.auth

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.taksapp.taksapp.R
import com.taksapp.taksapp.databinding.ActivityRiderLoginBinding
import com.taksapp.taksapp.ui.auth.viewmodels.LoginViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class RiderLoginActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModel(qualifier = named("RiderLoginViewModel"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityRiderLoginBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_rider_login)
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
            Toast.makeText(this, "Successful login", Toast.LENGTH_LONG).show()
        })
    }
}
