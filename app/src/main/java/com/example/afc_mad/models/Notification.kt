package com.example.afc_mad.models

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val type: String = "order", // order, review, general
    val relatedId: String = "" // orderId or reviewId
)
