package com.example.afc_mad.repository

import com.example.afc_mad.models.Notification
import com.example.afc_mad.models.Order
import com.google.firebase.database.*
import java.util.UUID

class OrderRepository {
    private val database = FirebaseDatabase.getInstance().getReference("orders")
    private val notificationsDb = FirebaseDatabase.getInstance().getReference("notifications")
    private val usersDb = FirebaseDatabase.getInstance().getReference("users")

    fun placeOrder(order: Order, onComplete: (Boolean, String?) -> Unit) {
        database.child(order.orderId).setValue(order)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendStatusNotification(order.userId, "Order Placed", "Your order ${order.orderId} has been placed successfully.", order.orderId)
                    // notifyAdmin removed to stop admin notifications
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, onComplete: (Boolean) -> Unit) {
        database.child(orderId).child("status").setValue(newStatus)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchOrderAndNotify(orderId, newStatus)
                }
                onComplete(task.isSuccessful)
            }
    }

    private fun fetchOrderAndNotify(orderId: String, status: String) {
        database.child(orderId).get().addOnSuccessListener { snapshot ->
            val order = snapshot.getValue(Order::class.java)
            if (order != null) {
                val message = when (status) {
                    "Accepted" -> "Your order is accepted and will be prepared shortly."
                    "Preparing" -> "Chef is preparing your delicious meal!"
                    "Ready" -> "Your order is ready for pickup/delivery."
                    "Assigned Driver" -> "A delivery partner has been assigned to your order."
                    "Out for Delivery" -> "Hang tight! Your order is on the way."
                    "Delivered" -> "Order delivered! Enjoy your meal."
                    "Cancelled" -> "Your order has been cancelled."
                    else -> "Your order status has changed to $status"
                }
                sendStatusNotification(order.userId, "Order Update", message, orderId)
            }
        }
    }

    private fun sendStatusNotification(userId: String, title: String, message: String, orderId: String) {
        val id = notificationsDb.child(userId).push().key ?: UUID.randomUUID().toString()
        val notification = Notification(id, title, message, System.currentTimeMillis(), false, "order", orderId)
        notificationsDb.child(userId).child(id).setValue(notification)
    }

    // Removed notifyAdmin function

    fun getOrdersForUser(userId: String, onUpdate: (List<Order>) -> Unit) {
        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders = mutableListOf<Order>()
                    for (child in snapshot.children) {
                        child.getValue(Order::class.java)?.let { orders.add(it) }
                    }
                    onUpdate(orders.sortedByDescending { it.createdAt })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getAllOrders(onUpdate: (List<Order>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()
                for (child in snapshot.children) {
                    child.getValue(Order::class.java)?.let { orders.add(it) }
                }
                onUpdate(orders.sortedByDescending { it.createdAt })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
