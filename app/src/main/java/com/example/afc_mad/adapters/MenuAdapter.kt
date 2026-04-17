package com.example.afc_mad.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ItemMenuBinding
import com.example.afc_mad.models.MenuItem
import java.io.File

class MenuAdapter(
    private var items: List<MenuItem>,
    private val onItemClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvPrice.text = "Rs ${item.price.toInt()}"
        holder.binding.tvCategory.text = item.category

        // FIX: reset image before loading to avoid recycled-view stale images
        holder.binding.ivFood.setImageBitmap(null)
        holder.binding.ivFood.setImageDrawable(null)

        loadImage(holder, item)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    private fun loadImage(holder: MenuViewHolder, item: MenuItem) {
        // FIX: treat null, empty string, AND the literal string "none" all as "no image"
        val path = item.imagePath
        if (path.isNullOrEmpty() || path == "none") {
            holder.binding.ivFood.setImageResource(android.R.drawable.ic_menu_gallery)
            return
        }

        try {
            when {
                // Case 1: absolute file path saved to internal storage — the main case
                path.startsWith("/") -> {
                    val imgFile = File(path)
                    if (imgFile.exists() && imgFile.length() > 0) {
                        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                        if (bitmap != null) {
                            holder.binding.ivFood.setImageBitmap(bitmap)
                        } else {
                            // File exists but is corrupt/unreadable
                            holder.binding.ivFood.setImageResource(android.R.drawable.ic_menu_report_image)
                        }
                    } else {
                        // File was deleted or path is stale
                        holder.binding.ivFood.setImageResource(android.R.drawable.ic_menu_report_image)
                    }
                }

                // Case 2: raw content:// URI — these expire, show placeholder
                path.startsWith("content://") -> {
                    holder.binding.ivFood.setImageResource(android.R.drawable.ic_menu_report_image)
                }

                // Case 3: drawable resource name (e.g. "krunch_burger" for bundled assets)
                else -> {
                    val context = holder.binding.root.context
                    val resourceId = context.resources.getIdentifier(path, "drawable", context.packageName)
                    if (resourceId != 0) {
                        holder.binding.ivFood.setImageResource(resourceId)
                    } else {
                        holder.binding.ivFood.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.binding.ivFood.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MenuItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
