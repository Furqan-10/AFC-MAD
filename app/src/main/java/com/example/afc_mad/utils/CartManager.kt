package com.example.afc_mad.utils

import android.content.Context
import com.example.afc_mad.models.CartItem
import com.example.afc_mad.models.MenuItem
import java.io.File

object CartManager {
    private val cartItems = mutableListOf<CartItem>()
    private const val CART_FILE = "cart.txt"

    fun init(context: Context, fileHandler: FileHandler) {
        val file = File(context.filesDir, CART_FILE)
        if (file.exists()) {
            cartItems.clear()
            val menuItems = fileHandler.getMenuItems().associateBy { it.id }
            file.readLines().forEach { line ->
                val parts = line.split("|")
                if (parts.size == 2) {
                    val menuItem = menuItems[parts[0]]
                    if (menuItem != null) {
                        cartItems.add(CartItem(menuItem, parts[1].toInt()))
                    }
                }
            }
        }
    }

    private fun saveCartLocally(context: Context) {
        val data = cartItems.joinToString("\n") { "${it.menuItem.id}|${it.quantity}" }
        context.openFileOutput(CART_FILE, Context.MODE_PRIVATE).use {
            it.write(data.toByteArray())
        }
    }

    fun addToCart(context: Context, menuItem: MenuItem) {
        val existingItem = cartItems.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(menuItem, 1))
        }
        saveCartLocally(context)
    }

    fun removeFromCart(context: Context, menuItem: MenuItem) {
        val existingItem = cartItems.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity--
            } else {
                cartItems.remove(existingItem)
            }
        }
        saveCartLocally(context)
    }

    fun getCartItems(): List<CartItem> = cartItems

    fun getTotalPrice(): Double = cartItems.sumOf { it.totalLinePrice }

    fun clearCart(context: Context) {
        cartItems.clear()
        saveCartLocally(context)
    }
}
