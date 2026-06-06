package com.example.afc_mad.models

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = "" // This will store the Base64 string or Storage URL
)
