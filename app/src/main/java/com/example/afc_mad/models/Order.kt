package com.example.afc_mad.models

import java.io.Serializable

data class Order(
    val orderId: String,
    val userPhone: String,
    val userAddress: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val paymentMethod: String,
    val status: String = "Pending"
) : Serializable
