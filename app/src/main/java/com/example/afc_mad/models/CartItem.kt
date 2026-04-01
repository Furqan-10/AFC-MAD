package com.example.afc_mad.models

import java.io.Serializable

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int
) : Serializable {
    val totalLinePrice: Double
        get() = menuItem.price * quantity
}
