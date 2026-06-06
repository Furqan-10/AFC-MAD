package com.example.afc_mad.models

import java.io.Serializable

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val phone: String = "",
    val address: String = "",
    val paymentMethod: String = "",
    val totalAmount: Double = 0.0,
    val status: String = "Placed",
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<OrderItem> = emptyList()
) : Serializable
