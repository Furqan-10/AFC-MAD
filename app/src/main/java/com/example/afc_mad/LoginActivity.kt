package com.example.afc_mad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivityLoginBinding
import com.example.afc_mad.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

            // Firebase Authentication
            if (phone == "admin" && pin == "1234") {
                // Keep hardcoded admin for quick access or migrate to DB
                startActivity(Intent(this, AdminHomeActivity::class.java))
                finish()
            } else {
                val email = "$phone@afc.com"
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pin)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = FirebaseAuth.getInstance().currentUser!!.uid
                            fetchUserDataAndNavigate(uid)
                        } else {
                            setLoading(false)
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            binding.tilPhone.error = "Invalid credentials"
                            binding.tilPin.error = "Invalid credentials"
                        }
                    }
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun fetchUserDataAndNavigate(uid: String) {
        FirebaseDatabase.getInstance().getReference("users").child(uid)
            .get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("user_phone", user.phone)
                        putString("user_address", user.address)
                        putString("user_role", user.role)
                        apply()
                    }
                    
                    if (user.role == "admin") {
                        startActivity(Intent(this, AdminHomeActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finish()
                } else {
                    setLoading(false)
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Error fetching user: ${it.message}", Toast.LENGTH_SHORT).show()
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
