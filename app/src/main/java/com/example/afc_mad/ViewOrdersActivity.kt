package com.example.afc_mad

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.OrderAdapter
import com.example.afc_mad.databinding.ActivityViewOrdersBinding
import com.example.afc_mad.models.Order
import com.example.afc_mad.repository.OrderRepository
import com.google.android.material.chip.Chip

class ViewOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewOrdersBinding
    private val orderRepository = OrderRepository()
    private lateinit var adapter: OrderAdapter
    private var allOrders = listOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupStatusFilter()
        setupSearch()
        loadOrders()

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(
            orders = mutableListOf(),
            isAdmin = true,
            onItemClick = { order ->
                // Navigate to details if needed
            },
            onStatusUpdateClick = { order ->
                updateOrderStatus(order)
            }
        )
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupStatusFilter() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, _ ->
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val selectedChipId = binding.chipGroupStatus.checkedChipId
        
        val statusFilter = if (selectedChipId != View.NO_ID) {
            binding.chipGroupStatus.findViewById<Chip>(selectedChipId).text.toString()
        } else "All"

        val filtered = allOrders.filter { order ->
            val matchesName = order.customerName.lowercase().contains(query) || 
                             order.phone.contains(query) || 
                             order.orderId.lowercase().contains(query)
            
            val matchesStatus = statusFilter == "All" || order.status.equals(statusFilter, ignoreCase = true)
            matchesName && matchesStatus
        }

        adapter.updateOrders(filtered)
        
        if (filtered.isEmpty()) {
            binding.tvEmptyOrders.visibility = View.VISIBLE
            binding.tvEmptyOrders.text = if (query.isEmpty() && statusFilter == "All") 
                "No orders found" else "No matching orders"
        } else {
            binding.tvEmptyOrders.visibility = View.GONE
        }
    }

    private fun loadOrders() {
        binding.pbLoading.visibility = View.VISIBLE
        orderRepository.getAllOrders { orders ->
            binding.pbLoading.visibility = View.GONE
            allOrders = orders
            applyFilters()
        }
    }

    private fun updateOrderStatus(order: Order) {
        val nextStatus = when (order.status) {
            "Placed" -> "Accepted"
            "Accepted" -> "Preparing"
            "Preparing" -> "Ready"
            "Ready" -> "Assigned Driver"
            "Assigned Driver" -> "Out for Delivery"
            "Out for Delivery" -> "Delivered"
            else -> return
        }

        orderRepository.updateOrderStatus(order.orderId, nextStatus) { success ->
            if (success) {
                Toast.makeText(this, "Order updated to $nextStatus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update order", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
