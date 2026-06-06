package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.OrderAdapter
import com.example.afc_mad.databinding.ActivityMyOrdersBinding
import com.example.afc_mad.models.Order
import com.example.afc_mad.repository.OrderRepository
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth

class MyOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyOrdersBinding
    private val orderRepository = OrderRepository()
    private lateinit var adapter: OrderAdapter
    private var allUserOrders = listOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabs()
        loadMyOrders()

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(mutableListOf(), isAdmin = false, onItemClick = { order ->
            // Goal 9: Open Order Detail screen
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("orderId", order.orderId)
            startActivity(intent)
        })
        binding.rvMyOrders.layoutManager = LinearLayoutManager(this)
        binding.rvMyOrders.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterOrders(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadMyOrders() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        binding.pbLoading.visibility = View.VISIBLE
        orderRepository.getOrdersForUser(userId) { orders ->
            binding.pbLoading.visibility = View.GONE
            allUserOrders = orders
            filterOrders(binding.tabLayout.selectedTabPosition)
        }
    }

    private fun filterOrders(tabPosition: Int) {
        val filtered = if (tabPosition == 0) {
            allUserOrders.filter { 
                !it.status.equals("Delivered", ignoreCase = true) && 
                !it.status.equals("Cancelled", ignoreCase = true) 
            }
        } else {
            allUserOrders.filter { 
                it.status.equals("Delivered", ignoreCase = true) || 
                it.status.equals("Cancelled", ignoreCase = true) 
            }
        }

        adapter.updateOrders(filtered)
        
        if (filtered.isEmpty()) {
            binding.tvEmptyOrders.visibility = View.VISIBLE
            binding.tvEmptyOrders.text = if (tabPosition == 0) 
                "No active orders" else "No order history"
        } else {
            binding.tvEmptyOrders.visibility = View.GONE
        }
    }
}
