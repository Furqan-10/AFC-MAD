package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.CartAdapter
import com.example.afc_mad.databinding.ActivityCartBinding
import com.example.afc_mad.utils.CartManager
import com.example.afc_mad.utils.FileHandler

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter
    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)
        // Ensure cart is initialized from local storage
        CartManager.init(this, fileHandler)

        setupRecyclerView()
        updateTotal()

        binding.btnEmptyCart.setOnClickListener {
            if (CartManager.getCartItems().isNotEmpty()) {
                CartManager.clearCart(this)
                adapter.updateItems(mutableListOf())
                updateTotal()
                Toast.makeText(this, "Bucket emptied", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCheckout.setOnClickListener {
            if (CartManager.getCartItems().isNotEmpty()) {
                startActivity(Intent(this, CheckoutActivity::class.java))
            } else {
                Toast.makeText(this, "Your bucket is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(CartManager.getCartItems()) {
            updateTotal()
        }
        binding.rvCart.layoutManager = LinearLayoutManager(this)
        binding.rvCart.adapter = adapter
    }

    private fun updateTotal() {
        binding.tvTotalAmount.text = "Rs ${CartManager.getTotalPrice().toInt()}"
    }
}
