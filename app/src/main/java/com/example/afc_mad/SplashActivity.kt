package com.example.afc_mad

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        hideSystemUI()

        val iconBg = findViewById<View>(R.id.ivIconBg)
        val iconFg = findViewById<ImageView>(R.id.ivIconFg)
        val textGroup = findViewById<LinearLayout>(R.id.textGroup)

        val scaleAnim = ScaleAnimation(
            0.8f, 1.0f, 0.8f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1200
            interpolator = AccelerateDecelerateInterpolator()
            fillAfter = true
        }
        iconFg.startAnimation(scaleAnim)

        val bgFadeAnim = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            startOffset = 200
            fillAfter = true
        }
        iconBg.startAnimation(bgFadeAnim)

        val textFadeAnim = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            startOffset = 600
            fillAfter = true
        }
        textGroup.startAnimation(textFadeAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, 2200)
    }

    private fun checkAuthAndNavigate() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val role = sharedPref.getString("user_role", "customer")
            
            val intent = if (role == "admin") {
                Intent(this, AdminHomeActivity::class.java)
            } else {
                Intent(this, HomeActivity::class.java)
            }
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }
}
