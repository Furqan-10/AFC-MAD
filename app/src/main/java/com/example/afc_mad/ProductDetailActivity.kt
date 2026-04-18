package com.example.afc_mad

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ActivityProductDetailBinding
import com.example.afc_mad.models.MenuItem
import com.example.afc_mad.utils.CartManager
import com.example.afc_mad.utils.FileHandler
import java.io.File

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var fileHandler: FileHandler
    private var quantity = 1
    private var currentItem: MenuItem? = null

    // Track addon quantities: Item ID -> Quantity
    private val addonQuantities = mutableMapOf<String, Int>()
    private val addonItemsMap = mutableMapOf<String, MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        currentItem = intent.getSerializableExtra("menu_item") as? MenuItem

        if (currentItem != null) {
            setupUI(currentItem!!)
            setupDrinksSection()
        }

        binding.ivBack.setOnClickListener { finish() }

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
            currentItem?.let { item ->
                // Add main item with specified quantity
                repeat(quantity) {
                    CartManager.addToCart(this, item.copy())
                }

                // Add selected addons with their specific quantities
                addonQuantities.forEach { (id, qty) ->
                    val addon = addonItemsMap[id]
                    if (addon != null && qty > 0) {
                        repeat(qty) {
                            CartManager.addToCart(this, addon.copy())
                        }
                    }
                }

                Toast.makeText(this, "Added to Bucket", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupUI(item: MenuItem) {
        binding.tvDetailName.text = item.name
        binding.tvOptionName.text = item.name
        binding.tvDetailDescription.text = item.description
        binding.tvDetailPriceLabel.text = "Rs ${item.price.toInt()}"

        loadImage(item)
        updateQuantityUI()
    }

    private fun setupDrinksSection() {
        val isCurrentItemADrink = currentItem?.category?.contains("Drink", ignoreCase = true) == true

        if (isCurrentItemADrink) {
            binding.tvDrinkSectionLabel.visibility = View.GONE
            binding.rvDrinks.visibility = View.GONE
            return
        }

        val allMenu = fileHandler.getMenuItems()
        val drinks = allMenu.filter { it.category.contains("Drink", ignoreCase = true) }

        if (drinks.isEmpty()) {
            binding.tvDrinkSectionLabel.visibility = View.GONE
            binding.rvDrinks.visibility = View.GONE
        } else {
            binding.tvDrinkSectionLabel.visibility = View.VISIBLE
            binding.rvDrinks.visibility = View.VISIBLE
            binding.rvDrinks.layoutManager = LinearLayoutManager(this)

            drinks.forEach { addonItemsMap[it.id] = it }

            binding.rvDrinks.adapter = AddonAdapter(drinks) { item, qty ->
                addonQuantities[item.id] = qty
                updateQuantityUI()
            }
        }
    }

    private fun updateQuantityUI() {
        binding.tvQuantity.text = quantity.toString()

        val mainTotalPrice = (currentItem?.price ?: 0.0) * quantity
        var addonTotalPrice = 0.0

        addonQuantities.forEach { (id, qty) ->
            val price = addonItemsMap[id]?.price ?: 0.0
            addonTotalPrice += (price * qty)
        }

        binding.tvDetailPrice.text = "Rs ${(mainTotalPrice + addonTotalPrice).toInt()}"
    }

    private fun loadImage(item: MenuItem) {
        val path = item.imagePath
        if (path.isNullOrEmpty() || path == "none") {
            binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
            return
        }

        try {
            if (path.startsWith("/")) {
                val imgFile = File(path)
                if (imgFile.exists() && imgFile.length() > 0) {
                    binding.ivProductImage.setImageURI(Uri.fromFile(imgFile))
                } else {
                    binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                val resourceId = resources.getIdentifier(path, "drawable", packageName)
                if (resourceId != 0) {
                    binding.ivProductImage.setImageResource(resourceId)
                } else {
                    binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        } catch (e: Exception) {
            binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }

    inner class AddonAdapter(
        private val list: List<MenuItem>,
        private val onQuantityChange: (MenuItem, Int) -> Unit
    ) : RecyclerView.Adapter<AddonAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_addon_simple, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.tvPrice.text = "Rs ${item.price.toInt()}"

            val currentQty = addonQuantities[item.id] ?: 0
            holder.tvQty.text = currentQty.toString()

            holder.btnPlus.setOnClickListener {
                val newQty = (addonQuantities[item.id] ?: 0) + 1
                addonQuantities[item.id] = newQty
                holder.tvQty.text = newQty.toString()
                onQuantityChange(item, newQty)
            }

            holder.btnMinus.setOnClickListener {
                val current = addonQuantities[item.id] ?: 0
                if (current > 0) {
                    val newQty = current - 1
                    addonQuantities[item.id] = newQty
                    holder.tvQty.text = newQty.toString()
                    onQuantityChange(item, newQty)
                }
            }
        }

        override fun getItemCount() = list.size

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvAddonName)
            val tvPrice: TextView = view.findViewById(R.id.tvAddonPrice)
            val tvQty: TextView = view.findViewById(R.id.tvAddonQuantity)
            val btnPlus: ImageButton = view.findViewById(R.id.btnAddonPlus)
            val btnMinus: ImageButton = view.findViewById(R.id.btnAddonMinus)
        }
    }
}
