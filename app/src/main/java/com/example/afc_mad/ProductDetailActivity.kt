package com.example.afc_mad

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivityProductDetailBinding
import com.example.afc_mad.models.MenuItem
import com.example.afc_mad.utils.CartManager
import java.io.File

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getSerializableExtra("menu_item") as? MenuItem

        if (item != null) {
            binding.tvDetailName.text = item.name
            binding.tvDetailDescription.text = item.description
            val priceStr = "Rs ${item.price.toInt()}"
            binding.tvDetailPrice.text = priceStr
            binding.tvDetailPriceLabel.text = "($priceStr)"

            loadImage(item)

            binding.btnPlus.setOnClickListener {
                quantity++
                updateQuantityUI()
            }

            binding.btnMinus.setOnClickListener {
                if (quantity > 1) {
                    quantity--
                    updateQuantityUI()
                }
            }

            binding.btnAddToCart.setOnClickListener {
                for (i in 1..quantity) {
                    CartManager.addToCart(item)
                }
                Toast.makeText(this, "${item.name} ($quantity) added to bucket", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun loadImage(item: MenuItem) {
        // FIX: same "none" guard as MenuAdapter
        val path = item.imagePath
        if (path.isNullOrEmpty() || path == "none") {
            binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
            return
        }

        try {
            when {
                path.startsWith("/") -> {
                    val imgFile = File(path)
                    if (imgFile.exists() && imgFile.length() > 0) {
                        binding.ivProductImage.setImageURI(Uri.fromFile(imgFile))
                    } else {
                        binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_report_image)
                    }
                }
                path.startsWith("content://") -> {
                    binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_report_image)
                }
                else -> {
                    val resourceId = resources.getIdentifier(path, "drawable", packageName)
                    if (resourceId != 0) {
                        binding.ivProductImage.setImageResource(resourceId)
                    } else {
                        binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }

    private fun updateQuantityUI() {
        binding.tvQuantity.text = quantity.toString()
    }
}
