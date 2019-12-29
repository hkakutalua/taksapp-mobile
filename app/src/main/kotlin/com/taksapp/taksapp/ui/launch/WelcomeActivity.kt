package com.taksapp.taksapp.ui.launch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.taksapp.taksapp.R
import com.taksapp.taksapp.ui.auth.DriverLoginActivity
import com.taksapp.taksapp.ui.auth.RiderLoginActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val startButton : MaterialButton = findViewById(R.id.button_start)
        val loginAsDriverButton : MaterialButton = findViewById(R.id.button_login_as_driver)

        startButton.setOnClickListener {
            startActivity(Intent(this, RiderLoginActivity::class.java))
        }

        loginAsDriverButton.setOnClickListener {
            startActivity(Intent(this, DriverLoginActivity::class.java))
        }
    }
}
