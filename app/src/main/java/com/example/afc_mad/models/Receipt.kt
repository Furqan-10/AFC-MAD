package com.example.afc_mad.models

import java.io.Serializable

data class Receipt(
    val orderId: String = "",
    val userId: String = "",
    val pdfBase64: String = "", // Changed from pdfUrl to pdfBase64
    val generatedAt: Long = System.currentTimeMillis()
) : Serializable
