package com.example.afc_mad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.models.Category
import com.google.firebase.database.*
import java.util.UUID

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var rgOrderType: RadioGroup
    private lateinit var rvCategories: RecyclerView
    private lateinit var etNewCategory: EditText
    private lateinit var btnAddCategory: Button
    private var selectedOrderType = "Delivery"
    private lateinit var adapter: CategoryAdapter
    
    private val database = FirebaseDatabase.getInstance().getReference("categories")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        rgOrderType = findViewById(R.id.rgOrderType)
        rvCategories = findViewById(R.id.rvCategories)
        etNewCategory = findViewById(R.id.etNewCategory)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadCategoriesFromFirebase()

        rgOrderType.setOnCheckedChangeListener { _, checkedId ->
            selectedOrderType = when (checkedId) {
                R.id.rbPickup -> "Pickup"
                R.id.rbMerch -> "Merch"
                else -> "Delivery"
            }
            loadCategoriesFromFirebase()
        }

        btnAddCategory.setOnClickListener {
            val name = etNewCategory.text.toString().trim()
            if (name.isNotEmpty()) {
                saveCategoryToFirebase(name)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(mutableListOf()) { category ->
            database.child(category.id).removeValue()
        }
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = adapter
    }

    private fun loadCategoriesFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Category>()
                for (child in snapshot.children) {
                    val cat = child.getValue(Category::class.java)
                    if (cat != null && cat.orderType == selectedOrderType) {
                        list.add(cat)
                    }
                }
                adapter.updateList(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageCategoriesActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun saveCategoryToFirebase(name: String) {
        val id = database.push().key ?: UUID.randomUUID().toString()
        val category = Category(id, name, selectedOrderType)
        
        database.child(id).setValue(category).addOnSuccessListener {
            etNewCategory.text.clear()
            Toast.makeText(this, "Category Added", Toast.LENGTH_SHORT).show()
        }
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
