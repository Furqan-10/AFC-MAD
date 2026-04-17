package com.example.afc_mad.models

import java.io.Serializable

data class MenuItem(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val imagePath: String? = null,
    val orderType: String = "Delivery" // "Delivery", "Pickup", "Merch"
) : Serializable
