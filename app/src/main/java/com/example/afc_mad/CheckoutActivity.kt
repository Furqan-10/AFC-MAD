package com.example.afc_mad

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivityCheckoutBinding
import com.example.afc_mad.models.Order
import com.example.afc_mad.utils.CartManager
import com.example.afc_mad.utils.FileHandler
import java.util.*

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileHandler = FileHandler(this)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val phone = sharedPref.getString("user_phone", "") ?: ""
        val address = sharedPref.getString("user_address", "") ?: ""

        binding.tvOrderSummary.text = "Shipping to: $address\nTotal: $${String.format("%.2f", CartManager.getTotalPrice())}"

        binding.btnPlaceOrder.setOnClickListener {
            val paymentMethod = if (binding.rbCash.isChecked) "Cash" else "Card"
            val order = Order(
                orderId = UUID.randomUUID().toString().substring(0, 8),
                userPhone = phone,
                userAddress = address,
                items = CartManager.getCartItems().toList(),
                totalPrice = CartManager.getTotalPrice(),
                paymentMethod = paymentMethod
            )

            fileHandler.saveOrder(order)
            CartManager.clearCart()
            Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
            
            val intent = android.content.Intent(this, HomeActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
