package com.example.afc_mad

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.MenuAdapter
import com.example.afc_mad.databinding.ActivityManageMenuBinding
import com.example.afc_mad.models.MenuItem
import com.example.afc_mad.utils.FileHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ManageMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageMenuBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var adapter: MenuAdapter
    private var savedImagePath: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            val path = saveImageToInternalStorage(uri)
            if (path != null) {
                savedImagePath = path
                binding.ivSelectedImage.setImageURI(Uri.fromFile(File(path)))
                binding.ivSelectedImage.imageTintList = null
                binding.tvImagePlaceholder.text = "Image Selected ✓"
            } else {
                Toast.makeText(this, "Failed to save image. Try again.", Toast.LENGTH_SHORT).show()
                binding.tvImagePlaceholder.text = "Image failed — tap to retry"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        setupRecyclerView()
        
        // Initial setup for category dropdown based on default "Delivery" selection
        refreshCategoryDropdown()

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Refresh category dropdown when order type changes
        binding.rgOrderType.setOnCheckedChangeListener { _, _ ->
            refreshCategoryDropdown()
        }

        binding.cardPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            pickImageLauncher.launch(intent)
        }

        binding.btnAddItem.setOnClickListener {
            val name = binding.etItemName.text.toString().trim()
            val priceStr = binding.etItemPrice.text.toString().trim()
            val desc = binding.etItemDesc.text.toString().trim()
            val category = binding.spinnerCategory.text.toString()
            
            // Get order type from RadioGroup (3 options: Delivery, Pickup, Merch)
            val orderType = when (binding.rgOrderType.checkedRadioButtonId) {
                R.id.rbPickup -> "Pickup"
                R.id.rbMerch -> "Merch"
                else -> "Delivery"
            }

            if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val price = priceStr.toDouble()
                val item = MenuItem(
                    id = UUID.randomUUID().toString().substring(0, 6),
                    name = name,
                    price = price,
                    description = desc,
                    category = category,
                    imagePath = savedImagePath,
                    orderType = orderType
                )
                fileHandler.saveMenuItem(item)
                Toast.makeText(this, "Product Added Successfully", Toast.LENGTH_SHORT).show()
                clearFields()
                updateList()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshCategoryDropdown() {
        val selectedOrderType = when (binding.rgOrderType.checkedRadioButtonId) {
            R.id.rbPickup -> "Pickup"
            R.id.rbMerch -> "Merch"
            else -> "Delivery"
        }
        
        // Fetch categories added via "Manage Categories" filtered by service type
        val categories = fileHandler.getCategories()
            .filter { it.orderType.equals(selectedOrderType, ignoreCase = true) }
            .map { it.name }
            .toTypedArray()
            
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.spinnerCategory.setAdapter(adapter)
        
        // Clear previous selection if it's not valid for the new type
        binding.spinnerCategory.text.clear()
        if (categories.isEmpty()) {
            binding.spinnerCategory.hint = "No categories for $selectedOrderType"
        } else {
            binding.spinnerCategory.hint = "Select Category"
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open URI stream")
            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0) file.absolutePath else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(fileHandler.getMenuItems()) { item ->
            if (!item.imagePath.isNullOrEmpty() && item.imagePath.startsWith("/")) {
                val imgFile = File(item.imagePath)
                if (imgFile.exists()) imgFile.delete()
            }
            fileHandler.deleteMenuItem(item.id)
            Toast.makeText(this, "Removed: ${item.name}", Toast.LENGTH_SHORT).show()
            updateList()
        }
        binding.rvAdminMenu.layoutManager = LinearLayoutManager(this)
        binding.rvAdminMenu.adapter = adapter
    }

    private fun updateList() {
        adapter.updateItems(fileHandler.getMenuItems())
    }

    private fun clearFields() {
        binding.etItemName.text?.clear()
        binding.etItemPrice.text?.clear()
        binding.etItemDesc.text?.clear()
        binding.spinnerCategory.text?.clear()
        binding.ivSelectedImage.setImageResource(android.R.drawable.ic_menu_camera)
        binding.tvImagePlaceholder.text = "Tap to Add Photo"
        savedImagePath = null
        binding.etItemName.requestFocus()
    }
}
