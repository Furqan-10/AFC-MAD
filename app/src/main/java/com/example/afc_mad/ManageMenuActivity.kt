package com.example.afc_mad

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.MenuAdapter
import com.example.afc_mad.databinding.ActivityManageMenuBinding
import com.example.afc_mad.models.Category
import com.example.afc_mad.models.Product
import com.example.afc_mad.utils.FileHandler
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.util.*

class ManageMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageMenuBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var adapter: MenuAdapter
    private var selectedImageUri: Uri? = null
    
    private val productsDb = FirebaseDatabase.getInstance().getReference("products")
    private val categoriesDb = FirebaseDatabase.getInstance().getReference("categories")

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data ?: return@registerForActivityResult
            binding.ivSelectedImage.setImageURI(selectedImageUri)
            binding.ivSelectedImage.imageTintList = null
            binding.tvImagePlaceholder.text = "Image Selected ✓"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        setupRecyclerView()
        loadProductsFromFirebase()
        refreshCategoryDropdown()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rgOrderType.setOnCheckedChangeListener { _, _ ->
            refreshCategoryDropdown()
        }

        binding.cardPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        binding.btnAddItem.setOnClickListener {
            val name = binding.etItemName.text.toString().trim()
            val priceStr = binding.etItemPrice.text.toString().trim()
            val desc = binding.etItemDesc.text.toString().trim()
            val category = binding.spinnerCategory.text.toString()

            if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveProductToFirebase(name, priceStr.toInt(), desc, category)
        }
    }

    private fun saveProductToFirebase(name: String, price: Int, desc: String, category: String) {
        setLoading(true)
        val base64Image = uriToBase64(selectedImageUri!!)
        
        if (base64Image == null) {
            setLoading(false)
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        val productId = productsDb.push().key ?: UUID.randomUUID().toString()
        val product = Product(productId, name, desc, price, category, base64Image)

        productsDb.child(productId).setValue(product)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(this, "Product Added Successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Database Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            val outputStream = ByteArrayOutputStream()
            // Compress significantly to stay under DB limits and improve performance
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadProductsFromFirebase() {
        productsDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productList = mutableListOf<Product>()
                for (child in snapshot.children) {
                    val product = child.getValue(Product::class.java)
                    if (product != null) productList.add(product)
                }
                adapter.updateItems(productList)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = MenuAdapter(mutableListOf(), isAdmin = true) { product ->
            productsDb.child(product.id).removeValue()
        }
        binding.rvAdminMenu.layoutManager = LinearLayoutManager(this)
        binding.rvAdminMenu.adapter = adapter
    }

    private fun refreshCategoryDropdown() {
        val selectedOrderType = when (binding.rgOrderType.checkedRadioButtonId) {
            R.id.rbPickup -> "Pickup"
            R.id.rbMerch -> "Merch"
            else -> "Delivery"
        }
        
        categoriesDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                for (child in snapshot.children) {
                    val cat = child.getValue(Category::class.java)
                    if (cat != null && cat.orderType == selectedOrderType) {
                        categories.add(cat.name)
                    }
                }
                val adapter = ArrayAdapter(this@ManageMenuActivity, android.R.layout.simple_dropdown_item_1line, categories)
                binding.spinnerCategory.setAdapter(adapter)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        binding.spinnerCategory.text.clear()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnAddItem.isEnabled = !isLoading
        binding.btnAddItem.text = if (isLoading) "Saving..." else "ADD PRODUCT"
    }

    private fun clearFields() {
        binding.etItemName.text?.clear()
        binding.etItemPrice.text?.clear()
        binding.etItemDesc.text?.clear()
        binding.spinnerCategory.text?.clear()
        binding.ivSelectedImage.setImageResource(android.R.drawable.ic_menu_camera)
        binding.tvImagePlaceholder.text = "Tap to Add Photo"
        selectedImageUri = null
    }
}
