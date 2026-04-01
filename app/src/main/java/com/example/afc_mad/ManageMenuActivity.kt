package com.example.afc_mad

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.MenuAdapter
import com.example.afc_mad.databinding.ActivityManageMenuBinding
import com.example.afc_mad.models.MenuItem
import com.example.afc_mad.utils.FileHandler
import java.util.*

class ManageMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageMenuBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var adapter: MenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        setupRecyclerView()

        binding.btnAddItem.setOnClickListener {
            val name = binding.etItemName.text.toString().trim()
            val priceStr = binding.etItemPrice.text.toString().trim()
            val desc = binding.etItemDesc.text.toString().trim()
            val category = binding.etItemCategory.text.toString().trim()

            if (name.isNotEmpty() && priceStr.isNotEmpty() && category.isNotEmpty()) {
                try {
                    val price = priceStr.toDouble()
                    val item = MenuItem(
                        id = UUID.randomUUID().toString().substring(0, 6),
                        name = name,
                        price = price,
                        description = desc,
                        category = category
                    )
                    fileHandler.saveMenuItem(item)
                    Toast.makeText(this, "Item Added: $name", Toast.LENGTH_SHORT).show()
                    clearFields()
                    updateList()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(fileHandler.getMenuItems()) { item ->
            fileHandler.deleteMenuItem(item.id)
            Toast.makeText(this, "Deleted: ${item.name}", Toast.LENGTH_SHORT).show()
            updateList()
        }
        binding.rvAdminMenu.layoutManager = LinearLayoutManager(this)
        binding.rvAdminMenu.adapter = adapter
    }

    private fun updateList() {
        adapter.updateItems(fileHandler.getMenuItems())
    }

    private fun clearFields() {
        binding.etItemName.text.clear()
        binding.etItemPrice.text.clear()
        binding.etItemDesc.text.clear()
        binding.etItemCategory.text.clear()
        binding.etItemName.requestFocus()
    }
}
