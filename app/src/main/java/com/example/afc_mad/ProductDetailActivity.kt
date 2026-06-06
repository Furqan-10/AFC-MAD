package com.example.afc_mad

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.afc_mad.adapters.ReviewAdapter
import com.example.afc_mad.databinding.ActivityProductDetailBinding
import com.example.afc_mad.models.Product
import com.example.afc_mad.repository.RatingRepository
import com.example.afc_mad.utils.CartManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private var quantity = 1
    private var currentProduct: Product? = null

    private val addonQuantities = mutableMapOf<String, Int>()
    private val addonItemsMap = mutableMapOf<String, Product>()
    private val productsDb = FirebaseDatabase.getInstance().getReference("products")
    private val ratingRepository = RatingRepository()
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getStringExtra("productId")

        if (productId != null) {
            fetchProductDetails(productId)
            setupReviewsRecyclerView(productId)
        } else {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show()
            finish()
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
            currentProduct?.let { product ->
                repeat(quantity) {
                    CartManager.addToCart(this, product)
                }

                addonQuantities.forEach { (id, qty) ->
                    val addon = addonItemsMap[id]
                    if (addon != null && qty > 0) {
                        repeat(qty) {
                            CartManager.addToCart(this, addon)
                        }
                    }
                }

                Toast.makeText(this, "Added to Bucket", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupReviewsRecyclerView(productId: String) {
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        binding.rvReviews.adapter = reviewAdapter

        ratingRepository.getProductReviews(productId) { reviews ->
            if (reviews.isEmpty()) {
                binding.tvReviewsLabel.visibility = View.GONE
                binding.rvReviews.visibility = View.GONE
            } else {
                binding.tvReviewsLabel.visibility = View.VISIBLE
                binding.rvReviews.visibility = View.VISIBLE
                reviewAdapter.updateList(reviews)
            }
        }
    }

    private fun fetchProductDetails(productId: String) {
        binding.pbLoading.visibility = View.VISIBLE
        productsDb.child(productId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentProduct = snapshot.getValue(Product::class.java)
                if (currentProduct != null) {
                    setupUI(currentProduct!!)
                    setupDrinksSection()
                    binding.pbLoading.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                    binding.bottomBar.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@ProductDetailActivity, "Product not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductDetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun setupUI(product: Product) {
        binding.tvDetailName.text = product.name
        binding.tvOptionName.text = product.name
        binding.tvDetailDescription.text = product.description
        binding.tvDetailPriceLabel.text = "Rs ${product.price}"

        // Update Rating UI
        binding.productRatingBar.rating = product.averageRating.toFloat()
        binding.tvRatingStats.text = String.format("(%.1f | %d reviews)", product.averageRating, product.totalRatings)

        if (product.imageUrl.startsWith("http")) {
            Glide.with(this)
                .load(product.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivProductImage)
        } else {
            try {
                val imageBytes = Base64.decode(product.imageUrl, Base64.DEFAULT)
                Glide.with(this)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.ivProductImage)
            } catch (e: Exception) {
                binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }
            
        updateQuantityUI()
    }

    private fun setupDrinksSection() {
        val isCurrentItemADrink = currentProduct?.categoryId?.contains("Drink", ignoreCase = true) == true

        if (isCurrentItemADrink) {
            binding.tvDrinkSectionLabel.visibility = View.GONE
            binding.rvDrinks.visibility = View.GONE
            return
        }

        productsDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val drinks = mutableListOf<Product>()
                    for (child in snapshot.children) {
                        val product = child.getValue(Product::class.java)
                        if (product != null && product.categoryId.contains("Drink", ignoreCase = true)) {
                            drinks.add(product)
                            addonItemsMap[product.id] = product
                        }
                    }
                    
                    if (drinks.isEmpty()) {
                        binding.tvDrinkSectionLabel.visibility = View.GONE
                        binding.rvDrinks.visibility = View.GONE
                    } else {
                        binding.tvDrinkSectionLabel.visibility = View.VISIBLE
                        binding.rvDrinks.visibility = View.VISIBLE
                        binding.rvDrinks.layoutManager = LinearLayoutManager(this@ProductDetailActivity)
                        binding.rvDrinks.adapter = AddonAdapter(drinks) { product, qty ->
                            addonQuantities[product.id] = qty
                            updateQuantityUI()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateQuantityUI() {
        binding.tvQuantity.text = quantity.toString()

        val mainTotalPrice = (currentProduct?.price ?: 0) * quantity
        var addonTotalPrice = 0

        addonQuantities.forEach { (id, qty) ->
            val price = addonItemsMap[id]?.price ?: 0
            addonTotalPrice += (price * qty)
        }

        binding.tvDetailPrice.text = "Rs ${mainTotalPrice + addonTotalPrice}"
    }

    inner class AddonAdapter(
        private val list: List<Product>,
        private val onQuantityChange: (Product, Int) -> Unit
    ) : RecyclerView.Adapter<AddonAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_addon_simple, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.tvPrice.text = "Rs ${item.price}"

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
