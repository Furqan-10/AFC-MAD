package com.example.afc_mad.models

import java.io.Serializable

data class CartItem(
    val product: Product,
    var quantity: Int
) : Serializable {
    val totalLinePrice: Double
        get() = (product.price * quantity).toDouble()
}
