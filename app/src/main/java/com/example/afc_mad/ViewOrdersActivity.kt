package com.example.afc_mad

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.OrderAdapter
import com.example.afc_mad.databinding.ActivityViewOrdersBinding
import com.example.afc_mad.utils.FileHandler

class ViewOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewOrdersBinding
    private lateinit var fileHandler: FileHandler
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val orders = fileHandler.getOrders().toMutableList()
        adapter = OrderAdapter(orders) { order ->
            // Logic for when "DELIVERED" is clicked
            fileHandler.deleteOrder(order.orderId)
            
            // Refresh list
            val updatedOrders = fileHandler.getOrders()
            adapter.updateOrders(updatedOrders)
            
            Toast.makeText(this, "Order delivered and removed", Toast.LENGTH_SHORT).show()
        }
        
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }
}
