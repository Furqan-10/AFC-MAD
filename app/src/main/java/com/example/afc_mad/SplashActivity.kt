package com.example.afc_mad

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

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Modern System UI Hiding
        hideSystemUI()

        val iconBg = findViewById<View>(R.id.ivIconBg)
        val iconFg = findViewById<ImageView>(R.id.ivIconFg)
        val textGroup = findViewById<LinearLayout>(R.id.textGroup)

        // 1. ScaleAnimation for Foreground (0.8 to 1.0)
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

        // 2. AlphaAnimation for Background Layer (Fade In)
        val bgFadeAnim = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            startOffset = 200
            fillAfter = true
        }
        iconBg.startAnimation(bgFadeAnim)

        // 3. AlphaAnimation for Text Group (Fade In)
        val textFadeAnim = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            startOffset = 600
            fillAfter = true
        }
        textGroup.startAnimation(textFadeAnim)

        // Navigation Logic: 2200ms delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2200)
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