package com.example.afc_mad.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ItemCartBinding
import com.example.afc_mad.models.CartItem
import com.example.afc_mad.utils.CartManager

class CartAdapter(
    private var items: List<CartItem>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        
        holder.binding.tvCartName.text = item.menuItem.name
        holder.binding.tvCartPrice.text = "Rs ${item.totalLinePrice.toInt()}"
        holder.binding.tvQuantity.text = item.quantity.toString()

        holder.binding.btnPlus.setOnClickListener {
            CartManager.addToCart(context, item.menuItem)
            notifyItemChanged(position)
            onUpdate()
        }

        holder.binding.btnMinus.setOnClickListener {
            val oldSize = items.size
            CartManager.removeFromCart(context, item.menuItem)
            
            if (items.size < oldSize) {
                // Item was removed from the list
                notifyDataSetChanged()
            } else {
                // Quantity was just decreased
                notifyItemChanged(position)
            }
            onUpdate()
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
