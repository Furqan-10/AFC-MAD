package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.afc_mad.adapters.MenuAdapter
import com.example.afc_mad.databinding.ActivityHomeBinding
import com.example.afc_mad.utils.FileHandler

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var adapter: MenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        setupRecyclerView()
        setupCategoryChips()

        binding.btnGoToCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        val items = fileHandler.getMenuItems()
        adapter = MenuAdapter(items) { item ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("menu_item", item)
            startActivity(intent)
        }
        binding.rvMenu.layoutManager = GridLayoutManager(this, 2)
        binding.rvMenu.adapter = adapter
    }

    private fun setupCategoryChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                adapter.updateItems(fileHandler.getMenuItems())
                return@setOnCheckedStateChangeListener
            }
            
            val checkedId = checkedIds.first()
            when (checkedId) {
                R.id.chipAll -> adapter.updateItems(fileHandler.getMenuItems())
                R.id.chipBurgers -> filterByCategory("Burgers")
                R.id.chipZinger -> filterByCategory("Zinger")
            }
        }
    }

    private fun filterByCategory(category: String) {
        val filtered = fileHandler.getMenuItems().filter { it.category == category }
        adapter.updateItems(filtered)
    }
}