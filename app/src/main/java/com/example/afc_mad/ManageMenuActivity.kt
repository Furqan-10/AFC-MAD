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
    private var savedImagePath: String? = null  // FIX: track the SAVED path, not the URI

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult

            // FIX: copy immediately on pick, save path right here — don't wait until "Add" is tapped
            val path = saveImageToInternalStorage(uri)
            if (path != null) {
                savedImagePath = path
                binding.ivSelectedImage.setImageURI(Uri.fromFile(File(path))) // show from file, not URI
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
        setupCategorySpinner()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.cardPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                // FIX: request persistable permission so URI stays valid longer
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            pickImageLauncher.launch(intent)
        }

        binding.btnAddItem.setOnClickListener {
            val name = binding.etItemName.text.toString().trim()
            val priceStr = binding.etItemPrice.text.toString().trim()
            val desc = binding.etItemDesc.text.toString().trim()
            val category = binding.spinnerCategory.text.toString()
            val orderType = if (binding.rbDelivery.isChecked) "Delivery" else "Pickup"

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
                    imagePath = savedImagePath,  // FIX: use already-saved path (null is fine — no image)
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

    // FIX: improved saveImageToInternalStorage with explicit error logging
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open URI stream — permission may have expired")
            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    val bytesCopied = input.copyTo(output)
                    if (bytesCopied == 0L) throw IOException("Copied 0 bytes — file may be empty")
                }
            }
            // Verify file was actually written
            if (file.exists() && file.length() > 0) {
                file.absolutePath
            } else {
                file.delete()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Deals", "Burgers", "Zinger", "Sides", "Drinks", "Desserts")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(fileHandler.getMenuItems()) { item ->
            // FIX: also delete the image file from internal storage when item is removed
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
        savedImagePath = null   // FIX: reset the saved path, not a URI
        binding.etItemName.requestFocus()
    }
}
