package com.example.afc_mad

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.afc_mad.adapters.BannerAdapter
import com.example.afc_mad.adapters.MenuAdapter
import com.example.afc_mad.databinding.ActivityHomeBinding
import com.example.afc_mad.utils.FileHandler
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var menuAdapter: MenuAdapter
    private var currentOrderType = "Delivery"
    private var currentCategory = "All"
    
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private val hideComingSoonHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        
        setupMenuRecyclerView()
        setupOrderTypeToggles()
        setupBanners()
        setupDrawer()
        refreshUI()
        updateLocationDisplay()

        binding.btnGoToCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.ivMenuIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.btnOrderNow.setOnClickListener {
            binding.homeScrollView.smoothScrollTo(0, binding.tvExploreMenuHeader.top)
        }
    }

    private fun setupDrawer() {
        val navigationView = binding.navigationView
        if (navigationView.headerCount == 0) {
            navigationView.inflateHeaderView(R.layout.layout_drawer_header)
        }
        
        val headerView = navigationView.getHeaderView(0)
        
        if (headerView != null) {
            val tvName = headerView.findViewById<TextView>(R.id.tvProfileName)
            val tvAddress = headerView.findViewById<TextView>(R.id.tvProfileAddress)
            val btnLogout = headerView.findViewById<MaterialButton>(R.id.btnLogout)
            val btnChangeAddress = headerView.findViewById<MaterialButton>(R.id.btnChangeAddress)

            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val phone = sharedPref.getString("user_phone", "Guest")
            val address = sharedPref.getString("user_address", "No address set")

            tvName?.text = "User: $phone"
            tvAddress?.text = address

            btnLogout?.setOnClickListener {
                with(sharedPref.edit()) {
                    clear()
                    apply()
                }
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

            btnChangeAddress?.setOnClickListener {
                showChangeAddressDialog()
            }
        }
    }

    private fun showChangeAddressDialog() {
        val input = EditText(this)
        input.hint = "Enter new address"

        AlertDialog.Builder(this)
            .setTitle("Change Address")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newAddress = input.text.toString().trim()
                if (newAddress.isNotEmpty()) {
                    val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("user_address", newAddress).apply()
                    updateLocationDisplay()
                    setupDrawer() // Update drawer info
                    Toast.makeText(this, "Address updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateLocationDisplay() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val address = sharedPref.getString("user_address", "No Address Selected")
        binding.tvLocation.text = address
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
        startAutoScroll()
        updateLocationDisplay()
        setupDrawer()
    }
    
    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    private fun setupMenuRecyclerView() {
        menuAdapter = MenuAdapter(mutableListOf()) { item ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("menu_item", item)
            startActivity(intent)
        }
        binding.rvMenu.layoutManager = GridLayoutManager(this, 2)
        binding.rvMenu.adapter = menuAdapter
    }

    private fun setupOrderTypeToggles() {
        val buttons = listOf(binding.btnDelivery, binding.btnPickup, binding.btnMerch)
        buttons.forEach { btn ->
            btn.setOnClickListener {
                if (btn.id == R.id.btnMerch) {
                    showComingSoon()
                    return@setOnClickListener
                }
                currentOrderType = btn.text.toString()
                updateToggleUI(btn)
                currentCategory = "All"
                refreshUI()
            }
        }
    }

    private fun showComingSoon() {
        hideComingSoonHandler.removeCallbacksAndMessages(null)
        binding.tvComingSoon.visibility = View.VISIBLE
        binding.tvComingSoon.alpha = 0f
        binding.tvComingSoon.animate().alpha(1f).setDuration(300).start()
        
        hideComingSoonHandler.postDelayed({
            binding.tvComingSoon.animate().alpha(0f).setDuration(300).withEndAction {
                binding.tvComingSoon.visibility = View.GONE
            }.start()
        }, 2000)
    }

    private fun setupBanners() {
        val banners = fileHandler.getBanners()
        if (banners.isNotEmpty()) {
            val adapter = BannerAdapter(banners)
            binding.viewPagerBanners.adapter = adapter
            startAutoScroll()
        }
    }
    
    private fun startAutoScroll() {
        val banners = fileHandler.getBanners()
        if (banners.size <= 1) return
        
        stopAutoScroll()
        autoScrollRunnable = object : Runnable {
            override fun run() {
                var current = binding.viewPagerBanners.currentItem
                current = (current + 1) % banners.size
                binding.viewPagerBanners.setCurrentItem(current, true)
                autoScrollHandler.postDelayed(this, 3000)
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable!!, 3000)
    }
    
    private fun stopAutoScroll() {
        autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
    }

    private fun updateToggleUI(selectedBtn: MaterialButton) {
        val buttons = listOf(binding.btnDelivery, binding.btnPickup, binding.btnMerch)
        buttons.forEach { btn ->
            if (btn == selectedBtn) {
                btn.setStrokeColorResource(R.color.afc_red)
                btn.setTextColor(ContextCompat.getColor(this, R.color.afc_white))
                btn.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.afc_red))
            } else {
                btn.setStrokeColorResource(R.color.afc_dark_grey)
                btn.setTextColor(ContextCompat.getColor(this, R.color.afc_text_secondary))
                btn.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.afc_text_secondary))
            }
        }
    }

    private fun refreshUI() {
        loadDynamicCategories()
        filterMenu()
    }

    private fun loadDynamicCategories() {
        binding.chipGroup.removeAllViews()
        addChip("All")
        val categories = fileHandler.getCategories().filter { 
            it.orderType.equals(currentOrderType, ignoreCase = true) 
        }
        categories.forEach { addChip(it.name) }
    }

    private fun addChip(name: String) {
        val chip = Chip(this).apply {
            id = View.generateViewId()
            text = name
            isCheckable = true
            isChecked = (name == currentCategory)
            setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.afc_white)))
            chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, 
                if (isChecked) R.color.afc_red else R.color.afc_dark_grey))
            chipStrokeWidth = 0f
            
            setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    currentCategory = name
                    (buttonView as Chip).chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.afc_red)
                    )
                    uncheckOthers(buttonView as Chip)
                    filterMenu()
                } else {
                    (buttonView as Chip).chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.afc_dark_grey)
                    )
                }
            }
        }
        binding.chipGroup.addView(chip)
    }

    private fun uncheckOthers(selectedChip: Chip) {
        for (i in 0 until binding.chipGroup.childCount) {
            val child = binding.chipGroup.getChildAt(i)
            if (child is Chip && child != selectedChip) {
                child.isChecked = false
            }
        }
    }

    private fun filterMenu() {
        val allItems = fileHandler.getMenuItems()
        val filtered = allItems.filter { 
            val typeMatch = it.orderType.equals(currentOrderType, ignoreCase = true)
            val categoryMatch = currentCategory == "All" || it.category.equals(currentCategory, ignoreCase = true)
            typeMatch && categoryMatch
        }
        menuAdapter.updateItems(filtered)
    }
}
