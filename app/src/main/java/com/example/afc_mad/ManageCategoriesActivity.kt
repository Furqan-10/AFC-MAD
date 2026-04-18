package com.example.afc_mad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.models.Category
import com.example.afc_mad.utils.FileHandler
import java.util.UUID

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var fileHandler: FileHandler
    private lateinit var rgOrderType: RadioGroup
    private lateinit var rvCategories: RecyclerView
    private lateinit var etNewCategory: EditText
    private lateinit var btnAddCategory: Button
    private var selectedOrderType = "Delivery"
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        fileHandler = FileHandler(this)

        rgOrderType = findViewById(R.id.rgOrderType)
        rvCategories = findViewById(R.id.rvCategories)
        etNewCategory = findViewById(R.id.etNewCategory)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadCategories()

        rgOrderType.setOnCheckedChangeListener { _, checkedId ->
            selectedOrderType = when (checkedId) {
                R.id.rbPickup -> "Pickup"
                R.id.rbMerch -> "Merch"
                else -> "Delivery"
            }
            loadCategories()
        }

        btnAddCategory.setOnClickListener {
            val name = etNewCategory.text.toString().trim()
            if (name.isNotEmpty()) {
                val exists = fileHandler.getCategories().any { 
                    it.name.equals(name, ignoreCase = true) && it.orderType == selectedOrderType 
                }
                if (!exists) {
                    val category = Category(UUID.randomUUID().toString(), name, selectedOrderType)
                    fileHandler.saveCategory(category)
                    etNewCategory.text.clear()
                    loadCategories()
                } else {
                    Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(mutableListOf()) { category ->
            fileHandler.deleteCategory(category.id)
            loadCategories()
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter
    }

    private fun loadCategories() {
        val categories = fileHandler.getCategories().filter { it.orderType == selectedOrderType }
        adapter.updateList(categories)
    }

    inner class CategoryAdapter(
        private var list: List<Category>,
        private val onDelete: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.VH>() {

        fun updateList(newList: List<Category>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.tvName.text = item.name
            holder.btnDelete.setOnClickListener { onDelete(item) }
            // Hide edit button for now to keep it simple
            holder.btnEdit.visibility = View.GONE
        }

        override fun getItemCount() = list.size

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvCategoryName)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteCategory)
            val btnEdit: ImageButton = view.findViewById(R.id.btnEditCategory)
        }
    }
}
