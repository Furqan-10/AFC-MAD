package com.example.afc_mad

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivitySignupBinding
import com.example.afc_mad.models.User
import com.example.afc_mad.utils.FileHandler

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)

        binding.btnSignup.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()

            // Reset errors
            binding.tilPhone.error = null
            binding.tilAddress.error = null
            binding.tilPin.error = null

            // Validation
            var isValid = true
            if (phone.isEmpty()) {
                binding.tilPhone.error = "Phone number is required"
                isValid = false
            }
            if (address.isEmpty()) {
                binding.tilAddress.error = "Address is required"
                isValid = false
            }
            if (pin.isEmpty() || pin.length < 4) {
                binding.tilPin.error = "PIN must be at least 4 digits"
                isValid = false
            }

            if (!isValid) return@setOnClickListener

            // Show loading state
            setLoading(true)

            // Simulate registration delay
            Handler(Looper.getMainLooper()).postDelayed({
                val newUser = User(phone, address, pin)
                fileHandler.saveUser(newUser)
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                finish()
            }, 1500)
        }
        
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnSignup.text = ""
            binding.btnSignup.isEnabled = false
            binding.pbLoading.visibility = View.VISIBLE
        } else {
            binding.btnSignup.text = "REGISTER"
            binding.btnSignup.isEnabled = true
            binding.pbLoading.visibility = View.GONE
        }
    }
}