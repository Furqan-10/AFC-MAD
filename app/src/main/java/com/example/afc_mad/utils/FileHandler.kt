package com.example.afc_mad.utils

import android.content.Context
import com.example.afc_mad.models.CartItem
import com.example.afc_mad.models.MenuItem
import com.example.afc_mad.models.Order
import com.example.afc_mad.models.User
import java.io.File

class FileHandler(private val context: Context) {

    private val usersFile = "users.txt"
    private val menuFile = "menu.txt"
    private val ordersFile = "orders.txt"

    fun saveUser(user: User) {
        val data = "${user.phone}|${user.address}|${user.pin}|${user.isAdmin}\n"
        context.openFileOutput(usersFile, Context.MODE_APPEND).use {
            it.write(data.toByteArray())
        }
    }

    fun getUsers(): List<User> {
        val users = mutableListOf<User>()
        val file = File(context.filesDir, usersFile)
        if (file.exists()) {
            file.readLines().forEach { line ->
                val parts = line.split("|")
                if (parts.size == 4) {
                    users.add(User(parts[0], parts[1], parts[2], parts[3].toBoolean()))
                }
            }
        }
        return users
    }

    fun saveMenuItem(item: MenuItem) {
        val data = "${item.id}|${item.name}|${item.price}|${item.description}|${item.category}\n"
        context.openFileOutput(menuFile, Context.MODE_APPEND).use {
            it.write(data.toByteArray())
        }
    }

    fun getMenuItems(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        val file = File(context.filesDir, menuFile)
        if (file.exists()) {
            file.readLines().forEach { line ->
                val parts = line.split("|")
                if (parts.size == 5) {
                    try {
                        items.add(MenuItem(parts[0], parts[1], parts[2].toDouble(), parts[3], parts[4]))
                    } catch (e: Exception) {
                        // Skip malformed lines
                    }
                }
            }
        }
        return items
    }

    fun deleteMenuItem(id: String) {
        val items = getMenuItems().filter { it.id != id }
        val file = File(context.filesDir, menuFile)
        // Correctly overwrite the file
        context.openFileOutput(menuFile, Context.MODE_PRIVATE).use {
            it.write("".toByteArray())
        }
        items.forEach { saveMenuItem(it) }
    }

    fun saveOrder(order: Order) {
        val itemsString = order.items.joinToString(";") { "${it.menuItem.id}:${it.quantity}" }
        val data = "${order.orderId}|${order.userPhone}|${order.userAddress}|$itemsString|${order.totalPrice}|${order.paymentMethod}|${order.status}\n"
        context.openFileOutput(ordersFile, Context.MODE_APPEND).use {
            it.write(data.toByteArray())
        }
    }

    fun getOrders(): List<Order> {
        val orders = mutableListOf<Order>()
        val menuItems = getMenuItems().associateBy { it.id }
        val file = File(context.filesDir, ordersFile)
        if (file.exists()) {
            file.readLines().forEach { line ->
                val parts = line.split("|")
                if (parts.size == 7) {
                    try {
                        val items = parts[3].split(";").mapNotNull {
                            val subParts = it.split(":")
                            if (subParts.size == 2) {
                                val menuItem = menuItems[subParts[0]]
                                if (menuItem != null) CartItem(menuItem, subParts[1].toInt()) else null
                            } else null
                        }
                        orders.add(Order(parts[0], parts[1], parts[2], items, parts[4].toDouble(), parts[5], parts[6]))
                    } catch (e: Exception) {
                        // Skip malformed lines
                    }
                }
            }
        }
        return orders
    }
}
