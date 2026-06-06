package com.example.afc_mad.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object TokenManager {
    fun updateToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("fcmToken")
                .setValue(token)
        }
    }
}
