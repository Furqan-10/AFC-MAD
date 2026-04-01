package com.example.afc_mad

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivityProductDetailBinding
import com.example.afc_mad.models.MenuItem
import com.example.afc_mad.utils.CartManager

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getSerializableExtra("menu_item") as? MenuItem

        if (item != null) {
            binding.tvDetailName.text = item.name
            binding.tvDetailCategory.text = item.category
            binding.tvDetailDescription.text = item.description
            binding.tvDetailPrice.text = "$${item.price}"

            binding.btnAddToCart.setOnClickListener {
                CartManager.addToCart(item)
                Toast.makeText(this, "${item.name} added to cart", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
