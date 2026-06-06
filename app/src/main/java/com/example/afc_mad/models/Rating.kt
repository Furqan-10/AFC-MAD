package com.example.afc_mad.models

data class Rating(
    val ratingId: String = "",
    val productId: String = "",
    val productName: String = "",
    val userId: String = "",
    val orderId: String = "",
    val userName: String = "",
    val stars: Int = 0,
    val review: String = "",
    val timestamp: Long = 0L
)
