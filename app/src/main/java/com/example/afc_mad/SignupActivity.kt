package com.example.afc_mad

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivitySignupBinding
import com.example.afc_mad.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()

            // Reset errors
            binding.tilName.error = null
            binding.tilPhone.error = null
            binding.tilAddress.error = null
            binding.tilPin.error = null

            // Validation
            var isValid = true
            if (name.isEmpty()) {
                binding.tilName.error = "Name is required"
                isValid = false
            }
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

            // Firebase Auth Registration
            // Using phone as email pseudo-identity for simplicity in this migration
            val email = "$phone@afc.com"
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pin)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val user = User(uid, name, phone, address, "customer")

                        // Save User Data to Realtime Database
                        FirebaseDatabase.getInstance().getReference("users")
                            .child(uid)
                            .setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    setLoading(false)
                                    Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        setLoading(false)
                        Toast.makeText(this, "Auth Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
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
