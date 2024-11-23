package com.clerami.universe.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.clerami.universe.R
import com.clerami.universe.ui.landing.LandingActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the splash screen theme
        setContentView(R.layout.activity_splash)

        // Delay for a few seconds before launching LandingActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LandingActivity::class.java)
            startActivity(intent)
            finish() // Remove SplashActivity from back stack
        }, 1000) // 2000ms = 2 seconds delay
    }
}
