package com.example.afc_mad.utils

import android.content.Context
import com.example.afc_mad.models.*
import java.io.File

class FileHandler(private val context: Context) {

    private val ordersFile = "orders.txt"

    // Legacy FileHandler updated to match the new Firebase models to fix compilation errors.
    // This allows the project to build while we transition fully to Firebase orders.

    fun saveOrder(order: Order) {
        val itemsString = order.items.joinToString(";") { "${it.productId}:${it.quantity}" }
        val data = "${order.orderId}|${order.phone}|${order.address}|$itemsString|${order.totalAmount}|${order.paymentMethod}|${order.status}\n"
        context.openFileOutput(ordersFile, Context.MODE_APPEND).use {
            it.write(data.toByteArray())
        }
    }

    fun deleteOrder(orderId: String) {
        val orders = getOrders().filter { it.orderId != orderId }
        context.openFileOutput(ordersFile, Context.MODE_PRIVATE).use {
            it.write("".toByteArray())
        }
        orders.forEach { saveOrder(it) }
    }

    fun getOrders(): List<Order> {
        val orders = mutableListOf<Order>()
        val file = File(context.filesDir, ordersFile)
        if (file.exists()) {
            file.readLines().forEach { line ->
                val parts = line.split("|")
                if (parts.size >= 7) {
                    try {
                        val items = parts[3].split(";").mapNotNull {
                            val subParts = it.split(":")
                            if (subParts.size == 2) {
                                // Creating OrderItem without all fields as we only store ID and Qty locally
                                OrderItem(productId = subParts[0], quantity = subParts[1].toInt())
                            } else null
                        }
                        orders.add(Order(
                            orderId = parts[0],
                            phone = parts[1],
                            address = parts[2],
                            items = items,
                            totalAmount = parts[4].toDouble(),
                            paymentMethod = parts[5],
                            status = parts[6]
                        ))
                    } catch (e: Exception) {
                        // Ignore malformed lines
                    }
                }
            }
        }
        return orders
    }
}
