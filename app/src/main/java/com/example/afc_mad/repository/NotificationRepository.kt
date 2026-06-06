package com.example.afc_mad.repository

import com.example.afc_mad.models.Notification
import com.example.afc_mad.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationRepository {
    private val database = FirebaseDatabase.getInstance().getReference("notifications")
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private val auth = FirebaseAuth.getInstance()

    fun getNotifications(onUpdate: (List<Notification>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onUpdate(emptyList())
            return
        }
        
        database.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = mutableListOf<Notification>()
                for (child in snapshot.children) {
                    child.getValue(Notification::class.java)?.let { notifications.add(it) }
                }
                onUpdate(notifications.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                onUpdate(emptyList())
            }
        })
    }

    fun markAsRead(notificationId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.child(uid).child(notificationId).child("read").setValue(true)
    }

    fun clearAll() {
        val uid = auth.currentUser?.uid ?: return
        database.child(uid).removeValue()
    }

    fun notifyAdmins(title: String, message: String, type: String, relatedId: String) {
        usersRef.orderByChild("role").equalTo("admin").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (adminSnapshot in snapshot.children) {
                    val adminUid = adminSnapshot.key ?: continue
                    val notificationId = database.child(adminUid).push().key ?: continue
                    val notification = Notification(
                        id = notificationId,
                        title = title,
                        message = message,
                        timestamp = System.currentTimeMillis(),
                        read = false,
                        type = type,
                        relatedId = relatedId
                    )
                    database.child(adminUid).child(notificationId).setValue(notification)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
