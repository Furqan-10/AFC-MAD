package com.example.afc_mad.utils

import com.example.afc_mad.models.CartItem
import com.example.afc_mad.models.MenuItem

object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    fun addToCart(menuItem: MenuItem) {
        val existingItem = cartItems.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(menuItem, 1))
        }
    }

    fun removeFromCart(menuItem: MenuItem) {
        val existingItem = cartItems.find { it.menuItem.id == menuItem.id }
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

    fun clearCart() {
        cartItems.clear()
    }
}
