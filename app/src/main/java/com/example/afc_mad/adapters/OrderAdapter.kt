package com.example.afc_mad.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ItemOrderBinding
import com.example.afc_mad.models.Order

class OrderAdapter(
    private var orders: MutableList<Order>,
    private val onDeliveredClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.tvOrderId.text = "Order ID: ${order.orderId}"
        holder.binding.tvOrderUser.text = "User: ${order.userPhone}\nAddress: ${order.userAddress}"
        
        // Group items to show total count of each unique item in the order
        val itemsSummary = order.items
            .groupBy { it.menuItem.id }
            .map { (id, items) -> 
                val name = items[0].menuItem.name
                val totalQty = items.sumOf { it.quantity }
                "$name x$totalQty"
            }
            .joinToString("\n")

        holder.binding.tvOrderItems.text = "Items:\n$itemsSummary"
        holder.binding.tvOrderTotal.text = "Total Amount: Rs ${order.totalPrice.toInt()}"
        holder.binding.tvOrderStatus.text = order.status

        holder.binding.btnDelivered.setOnClickListener {
            onDeliveredClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
