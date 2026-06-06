package com.example.afc_mad.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.afc_mad.AdminHomeActivity
import com.example.afc_mad.HomeActivity
import com.example.afc_mad.OrderDetailActivity
import com.example.afc_mad.R
import com.example.afc_mad.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val ACTION_NOTIFICATION_RECEIVED = "com.example.afc_mad.NOTIFICATION_RECEIVED"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateTokenInDatabase(token)
    }

    private fun updateTokenInDatabase(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users")
            .child(uid)
            .child("fcmToken")
            .setValue(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "AFC Notification"
        val message = data["message"] ?: remoteMessage.notification?.body ?: ""
        val type = data["type"] ?: "order"
        val relatedId = data["relatedId"] ?: ""

        // Save to DB history
        saveNotificationToFirebase(title, message, type, relatedId)

        // Show system tray notification
        showSystemNotification(title, message, type, relatedId)

        // Send broadcast for foreground popup
        val intent = Intent(ACTION_NOTIFICATION_RECEIVED)
        intent.setPackage(packageName)
        intent.putExtra("title", title)
        intent.putExtra("message", message)
        sendBroadcast(intent)
    }

    private fun saveNotificationToFirebase(title: String, message: String, type: String, relatedId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(uid)
        val id = notificationsRef.push().key ?: return
        
        val notification = Notification(
            id = id,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            read = false,
            type = type,
            relatedId = relatedId
        )
        notificationsRef.child(id).setValue(notification)
    }

    private fun showSystemNotification(title: String, message: String, type: String, relatedId: String) {
        val channelId = "afc_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "AFC Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = when (type) {
            "admin_order" -> Intent(this, AdminHomeActivity::class.java)
            "order" -> Intent(this, OrderDetailActivity::class.java).apply {
                putExtra("orderId", relatedId)
            }
            else -> Intent(this, HomeActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
