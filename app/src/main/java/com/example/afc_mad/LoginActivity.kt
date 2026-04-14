package com.example.afc_mad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivityLoginBinding
import com.example.afc_mad.utils.FileHandler

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)

        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()

            // Reset errors
            binding.tilPhone.error = null
            binding.tilPin.error = null

            // Validation
            if (phone.isEmpty()) {
                binding.tilPhone.error = "Phone number is required"
                return@setOnClickListener
            }
            if (pin.isEmpty()) {
                binding.tilPin.error = "PIN is required"
                return@setOnClickListener
            }

            // Show loading state
            setLoading(true)

            // Simulate authentication delay
            Handler(Looper.getMainLooper()).postDelayed({
                if (phone == "admin" && pin == "1234") {
                    startActivity(Intent(this, AdminHomeActivity::class.java))
                    finish()
                } else {
                    val user = fileHandler.getUsers().find { it.phone == phone && it.pin == pin }
                    if (user != null) {
                        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("user_phone", user.phone)
                            putString("user_address", user.address)
                            apply()
                        }
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        setLoading(false)
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        binding.tilPhone.error = "Check credentials"
                        binding.tilPin.error = "Check credentials"
                    }
                }
            }, 1500)
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnLogin.text = ""
            binding.btnLogin.isEnabled = false
            binding.pbLoading.visibility = View.VISIBLE
        } else {
            binding.btnLogin.text = "LOGIN"
            binding.btnLogin.isEnabled = true
            binding.pbLoading.visibility = View.GONE
        }
    }
}