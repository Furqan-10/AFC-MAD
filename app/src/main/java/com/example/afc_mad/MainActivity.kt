package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Product initialization is now handled via Admin panel and Firebase
        
        // Redirect to Splash
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}
