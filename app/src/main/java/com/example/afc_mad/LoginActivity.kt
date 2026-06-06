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
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val pin = binding.etPin.text.toString().trim()

            if (phone.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)

            if (phone == "admin" && pin == "1234") {
                loginAdmin()
            } else {
                val email = "$phone@afc.com"
                val password = if (pin.length == 4) pin + "afc00" else pin
                
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateTokenAndNavigate(FirebaseAuth.getInstance().currentUser!!.uid)
                        } else {
                            setLoading(false)
                            Toast.makeText(this, "Login Failed: Check Phone/PIN", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun loginAdmin() {
        val adminEmail = "admin@afc.com"
        val adminPass = "admin123"

        FirebaseAuth.getInstance().signInWithEmailAndPassword(adminEmail, adminPass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    // Sync Admin profile to DB to enable notification history
                    val adminUser = User(uid, "Administrator", "admin", "Office", "admin")
                    FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(adminUser)
                        .addOnCompleteListener { updateTokenAndNavigate(uid) }
                } else {
                    // Create master admin if first time
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(adminEmail, adminPass)
                        .addOnCompleteListener { createAdminTask ->
                            if (createAdminTask.isSuccessful) {
                                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                                val adminUser = User(uid, "Administrator", "admin", "Office", "admin")
                                FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(adminUser)
                                    .addOnCompleteListener { updateTokenAndNavigate(uid) }
                            } else {
                                setLoading(false)
                                Toast.makeText(this, "Admin Auth Error", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
    }

    private fun updateTokenAndNavigate(uid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val token = if (task.isSuccessful) task.result else ""
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("fcmToken")
                .setValue(token).addOnCompleteListener {
                    fetchUserDataAndNavigate(uid)
                }
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
                        putString("user_name", user.name)
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
                    Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Error fetching user profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.text = if (isLoading) "" else "LOGIN"
        binding.btnLogin.isEnabled = !isLoading
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
