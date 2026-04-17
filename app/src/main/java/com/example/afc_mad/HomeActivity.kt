package com.example.afc_mad

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.afc_mad.adapters.MenuAdapter
import com.example.afc_mad.databinding.ActivityHomeBinding
import com.example.afc_mad.models.MenuItem
import com.example.afc_mad.utils.FileHandler
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var adapter: MenuAdapter
    private var currentOrderType = "Delivery"
    private var currentCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        setupRecyclerView()
        setupOrderTypeToggles()
        refreshUI()

        binding.btnGoToCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(mutableListOf()) { item ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("menu_item", item)
            startActivity(intent)
        }
        binding.rvMenu.layoutManager = GridLayoutManager(this, 2)
        binding.rvMenu.adapter = adapter
    }

    private fun setupOrderTypeToggles() {
        val buttons = listOf(binding.btnDelivery, binding.btnPickup, binding.btnMerch)
        buttons.forEach { btn ->
            btn.setOnClickListener {
                currentOrderType = btn.text.toString()
                updateToggleUI(btn)
                currentCategory = "All"
                refreshUI()
            }
        }
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
        
        // Add "All" Chip
        addChip("All")

        // Add dynamic categories from storage based on orderType
        val categories = fileHandler.getCategories().filter { it.orderType == currentOrderType }
        categories.forEach { addChip(it.name) }

        // Set listener for selection
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = findViewById<Chip>(checkedIds.first())
                currentCategory = chip.text.toString()
                filterMenu()
            }
        }
    }

    private fun addChip(name: String) {
        val chip = Chip(this).apply {
            text = name
            isCheckable = true
            isClickable = true
            isChecked = (name == currentCategory)
            setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.afc_white)))
            setChipBackgroundColorResource(if (isChecked) R.color.afc_red else R.color.afc_dark_grey)
            chipStrokeWidth = 0f
            
            setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    buttonView.setChipBackgroundColorResource(R.color.afc_red)
                } else {
                    buttonView.setChipBackgroundColorResource(R.color.afc_dark_grey)
                }
            }
        }
        binding.chipGroup.addView(chip)
    }

    private fun filterMenu() {
        val allItems = fileHandler.getMenuItems()
        val filtered = allItems.filter { 
            val typeMatch = it.orderType == currentOrderType
            val categoryMatch = currentCategory == "All" || it.category == currentCategory
            typeMatch && categoryMatch
        }
        adapter.updateItems(filtered)
    }
}