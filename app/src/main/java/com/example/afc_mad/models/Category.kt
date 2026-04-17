package com.example.afc_mad.models

import java.io.Serializable

data class Category(
    val id: String,
    val name: String,
    val orderType: String // "Delivery", "Pickup", "Merch"
) : Serializable
