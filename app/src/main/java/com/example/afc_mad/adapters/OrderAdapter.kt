package com.example.afc_mad.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ItemOrderBinding
import com.example.afc_mad.models.Order

class OrderAdapter(private val orders: List<Order>) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.tvOrderId.text = "Order ID: ${order.orderId}"
        holder.binding.tvOrderUser.text = "User: ${order.userPhone}\nAddress: ${order.userAddress}"
        
        val itemsSummary = order.items.joinToString("\n") { 
            "${it.menuItem.name} x${it.quantity} - $${it.totalLinePrice}" 
        }
        holder.binding.tvOrderItems.text = itemsSummary
        holder.binding.tvOrderTotal.text = "Total: $${String.format("%.2f", order.totalPrice)}"
        holder.binding.tvOrderStatus.text = order.status
    }

    override fun getItemCount(): Int = orders.size
}
