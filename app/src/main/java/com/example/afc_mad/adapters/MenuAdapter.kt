package com.example.afc_mad.adapters

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.afc_mad.databinding.ItemMenuBinding
import com.example.afc_mad.databinding.ItemMenuAdminBinding
import com.example.afc_mad.models.Product

class MenuAdapter(
    private var items: List<Product>,
    private val isAdmin: Boolean = false,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (isAdmin) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            val binding = ItemMenuAdminBinding.inflate(inflater, parent, false)
            AdminViewHolder(binding)
        } else {
            val binding = ItemMenuBinding.inflate(inflater, parent, false)
            CustomerViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is AdminViewHolder) {
            holder.binding.tvName.text = item.name
            holder.binding.tvPrice.text = "Rs ${item.price}"
            holder.binding.tvCategory.text = item.categoryId
            
            try {
                val imageBytes = Base64.decode(item.imageUrl, Base64.DEFAULT)
                Glide.with(holder.binding.ivFood.context)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.binding.ivFood)
            } catch (e: Exception) {
                holder.binding.ivFood.setImageResource(android.R.drawable.ic_menu_report_image)
            }
            
            holder.binding.btnRemoveItem.setOnClickListener { onItemClick(item) }
        } else if (holder is CustomerViewHolder) {
            holder.binding.tvName.text = item.name
            holder.binding.tvPrice.text = "Rs ${item.price}"
            holder.binding.tvCategory.text = item.categoryId
            
            try {
                val imageBytes = Base64.decode(item.imageUrl, Base64.DEFAULT)
                Glide.with(holder.binding.ivFood.context)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.binding.ivFood)
            } catch (e: Exception) {
                holder.binding.ivFood.setImageResource(android.R.drawable.ic_menu_report_image)
            }
            
            holder.itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class AdminViewHolder(val binding: ItemMenuAdminBinding) : RecyclerView.ViewHolder(binding.root)
    inner class CustomerViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root)
}
