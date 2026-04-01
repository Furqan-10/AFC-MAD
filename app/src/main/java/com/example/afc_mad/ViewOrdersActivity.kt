package com.example.afc_mad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.OrderAdapter
import com.example.afc_mad.databinding.ActivityViewOrdersBinding
import com.example.afc_mad.utils.FileHandler

class ViewOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewOrdersBinding
    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        
        val orders = fileHandler.getOrders()
        val adapter = OrderAdapter(orders)
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }
}
