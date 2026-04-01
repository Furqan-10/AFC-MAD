package com.example.afc_mad.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ItemCartBinding
import com.example.afc_mad.models.CartItem

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
        holder.binding.tvCartName.text = item.menuItem.name
        holder.binding.tvCartPrice.text = "$${item.totalLinePrice}"
        holder.binding.tvQuantity.text = item.quantity.toString()

        holder.binding.btnPlus.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
            onUpdate()
        }

        holder.binding.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
            } else {
                // In a real app, you might want to remove it from the list
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
