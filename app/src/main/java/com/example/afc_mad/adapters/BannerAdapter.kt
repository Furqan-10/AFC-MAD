package com.example.afc_mad.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ItemBannerBinding
import com.example.afc_mad.models.Banner
import java.io.File

class BannerAdapter(private val banners: List<Banner>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(val binding: ItemBannerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = banners[position]
        val file = File(banner.imagePath)
        if (file.exists()) {
            holder.binding.ivBanner.setImageURI(Uri.fromFile(file))
        }
    }

    override fun getItemCount(): Int = banners.size
}
