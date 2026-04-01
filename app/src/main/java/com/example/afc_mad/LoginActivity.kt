package com.example.afc_mad

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
            val phone = binding.etPhone.text.toString()
            val pin = binding.etPin.text.toString()

            if (phone == "admin" && pin == "1234") {
                startActivity(Intent(this, AdminHomeActivity::class.java))
                finish()
                return@setOnClickListener
            }

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
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
