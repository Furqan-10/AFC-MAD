package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
        binding.rvMenu.layoutManager = LinearLayoutManager(this)
        binding.rvMenu.adapter = adapter
    }

    private fun setupCategoryChips() {
        binding.chipAll.setOnClickListener { adapter.updateItems(fileHandler.getMenuItems()) }
        binding.chipBurgers.setOnClickListener { filterByCategory("Burgers") }
        binding.chipPizza.setOnClickListener { filterByCategory("Pizza") }
    }

    private fun filterByCategory(category: String) {
        val filtered = fileHandler.getMenuItems().filter { it.category == category }
        adapter.updateItems(filtered)
    }
}
