package com.example.afc_mad.models

import java.io.Serializable

data class Category(
    var id: String = "",
    var name: String = "",
    var orderType: String = "Delivery" // "Delivery", "Pickup", "Merch"
) : Serializable
