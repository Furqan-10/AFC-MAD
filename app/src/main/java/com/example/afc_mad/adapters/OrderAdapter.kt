package com.example.afc_mad.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ItemOrderBinding
import com.example.afc_mad.models.Order

class OrderAdapter(
    private var orders: MutableList<Order>,
    private val isAdmin: Boolean = false,
    private val onItemClick: ((Order) -> Unit)? = null,
    private val onStatusUpdateClick: ((Order) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        
        holder.binding.apply {
            tvOrderId.text = "Order ID: ${order.orderId}"
            tvOrderUser.text = "Customer: ${order.customerName}\nPhone: ${order.phone}"
            
            val itemsSummary = order.items.joinToString("\n") { 
                "${it.productName} x${it.quantity}"
            }
            tvOrderItems.text = "Items:\n$itemsSummary"
            tvOrderTotal.text = "Total: Rs ${order.totalAmount.toInt()}"
            tvOrderStatus.text = order.status

            if (isAdmin) {
                btnDelivered.visibility = if (order.status == "Delivered" || order.status == "Cancelled") View.GONE else View.VISIBLE
                btnDelivered.text = getNextStatusAction(order.status)
                btnDelivered.setOnClickListener { onStatusUpdateClick?.invoke(order) }
            } else {
                btnDelivered.visibility = View.GONE
            }

            root.setOnClickListener { onItemClick?.invoke(order) }
        }
    }

    private fun getNextStatusAction(currentStatus: String): String {
        return when (currentStatus) {
            "Placed" -> "Accept Order"
            "Accepted" -> "Start Preparing"
            "Preparing" -> "Mark Ready"
            "Ready" -> "Assign Driver"
            "Assigned Driver" -> "Send for Delivery"
            "Out for Delivery" -> "Mark Delivered"
            else -> "View Details"
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
