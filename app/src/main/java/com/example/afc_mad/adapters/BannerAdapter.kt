package com.example.afc_mad.adapters

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.afc_mad.databinding.ItemBannerBinding
import com.example.afc_mad.models.Banner

class BannerAdapter(private val banners: List<Banner>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(val binding: ItemBannerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = banners[position]
        try {
            val imageBytes = Base64.decode(banner.imagePath, Base64.DEFAULT)
            Glide.with(holder.binding.ivBanner.context)
                .asBitmap()
                .load(imageBytes)
                .into(holder.binding.ivBanner)
        } catch (e: Exception) {
            holder.binding.ivBanner.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }

    override fun getItemCount(): Int = banners.size
}
