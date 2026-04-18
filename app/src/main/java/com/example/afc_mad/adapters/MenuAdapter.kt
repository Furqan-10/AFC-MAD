package com.example.afc_mad.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.R
import com.example.afc_mad.databinding.ItemMenuBinding
import com.example.afc_mad.databinding.ItemMenuAdminBinding
import com.example.afc_mad.models.MenuItem
import java.io.File

class MenuAdapter(
    private var items: List<MenuItem>,
    private val isAdmin: Boolean = false,
    private val onItemClick: (MenuItem) -> Unit
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
            holder.binding.tvPrice.text = "Rs ${item.price.toInt()}"
            holder.binding.tvCategory.text = item.category
            loadImage(holder.binding.ivFood, item.imagePath)
            
            // Admin-only: clicking the remove icon deletes the item
            holder.binding.btnRemoveItem.setOnClickListener { onItemClick(item) }
        } else if (holder is CustomerViewHolder) {
            holder.binding.tvName.text = item.name
            holder.binding.tvPrice.text = "Rs ${item.price.toInt()}"
            holder.binding.tvCategory.text = item.category
            loadImage(holder.binding.ivFood, item.imagePath)
            
            // Customer: clicking the whole card opens details
            holder.itemView.setOnClickListener { onItemClick(item) }
        }
    }

    private fun loadImage(imageView: android.widget.ImageView, path: String?) {
        imageView.setImageBitmap(null)
        imageView.setImageDrawable(null)

        if (path.isNullOrEmpty() || path == "none") {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            return
        }

        try {
            if (path.startsWith("/")) {
                val imgFile = File(path)
                if (imgFile.exists() && imgFile.length() > 0) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    if (bitmap != null) imageView.setImageBitmap(bitmap)
                    else imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                val context = imageView.context
                val resourceId = context.resources.getIdentifier(path, "drawable", context.packageName)
                if (resourceId != 0) imageView.setImageResource(resourceId)
                else imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } catch (e: Exception) {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MenuItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class AdminViewHolder(val binding: ItemMenuAdminBinding) : RecyclerView.ViewHolder(binding.root)
    inner class CustomerViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root)
}
