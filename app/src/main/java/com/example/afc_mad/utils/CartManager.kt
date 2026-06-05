package com.example.afc_mad.utils

import android.content.Context
import com.example.afc_mad.models.CartItem
import com.example.afc_mad.models.Product

object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    fun addToCart(context: Context, product: Product) {
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(product, 1))
        }
    }

    fun removeFromCart(context: Context, product: Product) {
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity--
            } else {
                cartItems.remove(existingItem)
            }
        }
    }

    fun getCartItems(): List<CartItem> = cartItems

    fun getTotalPrice(): Double = cartItems.sumOf { it.totalLinePrice }

    fun clearCart(context: Context) {
        cartItems.clear()
    }
}
