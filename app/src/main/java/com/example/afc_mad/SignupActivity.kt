package com.example.afc_mad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivitySignupBinding
import com.example.afc_mad.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

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

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin.length < 6) {
                binding.tilPin.error = "PIN must be at least 6 digits"
                return@setOnClickListener
            }

            setLoading(true)

            val email = "$phone@afc.com"
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pin)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        saveUserToDb(uid, name, phone, address)
                    } else {
                        setLoading(false)
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
        
        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun saveUserToDb(uid: String, name: String, phone: String, address: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
            val fcmToken = if (tokenTask.isSuccessful) tokenTask.result else ""
            val user = User(uid, name, phone, address, "customer", fcmToken)

            FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(user)
                .addOnCompleteListener { dbTask ->
                    setLoading(false)
                    if (dbTask.isSuccessful) {
                        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("user_phone", phone)
                            putString("user_address", address)
                            putString("user_role", "customer")
                            putString("user_name", name)
                            apply()
                        }
                        
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSignup.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
