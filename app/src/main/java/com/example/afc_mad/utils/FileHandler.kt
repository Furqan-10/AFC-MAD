package com.example.afc_mad.utils

import android.content.Context
import com.example.afc_mad.models.*
import java.io.File

class FileHandler(private val context: Context) {

    private val ordersFile = "orders.txt"

    // Categories and Products are now in Firebase. 
    // This class is kept for any remaining local order/session persistence if needed, 
    // though those should ideally move to Firebase next.

    fun saveOrder(order: Order) {
        val itemsString = order.items.joinToString(";") { "${it.product.id}:${it.quantity}" }
        val data = "${order.orderId}|${order.userPhone}|${order.userAddress}|$itemsString|${order.totalPrice}|${order.paymentMethod}|${order.status}\n"
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
        // Note: Order reconstruction now needs Product objects which are in Firebase.
        // Full Order migration to Firebase is recommended next.
        return orders
    }
}
