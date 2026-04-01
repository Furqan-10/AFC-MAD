package com.example.afc_mad

import android.os.Bundle
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
            val phone = binding.etPhone.text.toString()
            val address = binding.etAddress.text.toString()
            val pin = binding.etPin.text.toString()

            if (phone.isNotEmpty() && address.isNotEmpty() && pin.length == 4) {
                val newUser = User(phone, address, pin)
                fileHandler.saveUser(newUser)
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
