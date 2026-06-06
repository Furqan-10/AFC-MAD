package com.example.afc_mad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivityCheckoutBinding
import com.example.afc_mad.models.Order
import com.example.afc_mad.models.OrderItem
import com.example.afc_mad.repository.OrderRepository
import com.example.afc_mad.repository.ReceiptRepository
import com.example.afc_mad.utils.CartManager
import com.example.afc_mad.utils.PdfGenerator
import com.example.afc_mad.utils.QrGenerator
import com.google.firebase.auth.FirebaseAuth

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private val orderRepository = OrderRepository()
    private val receiptRepository = ReceiptRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val phone = sharedPref.getString("user_phone", "") ?: ""
        val address = sharedPref.getString("user_address", "Not provided") ?: "Not provided"
        val name = sharedPref.getString("user_name", "Customer") ?: "Customer"

        binding.tvOrderAddress.text = "Address: $address"
        binding.tvOrderTotal.text = "Total: Rs ${CartManager.getTotalPrice().toInt()}"

        binding.btnPlaceOrder.setOnClickListener {
            placeOrder(phone, address, name)
        }
        
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun placeOrder(phone: String, address: String, name: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        val orderItems = CartManager.getCartItems().map { cartItem ->
            OrderItem(
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                quantity = cartItem.quantity,
                price = cartItem.product.price.toDouble(),
                imageUrl = cartItem.product.imageUrl
            )
        }

        val order = Order(
            orderId = "AFC-${System.currentTimeMillis().toString().takeLast(6)}",
            userId = currentUser.uid,
            customerName = name,
            phone = phone,
            address = address,
            paymentMethod = "Cash on Delivery",
            totalAmount = CartManager.getTotalPrice(),
            status = "Placed",
            createdAt = System.currentTimeMillis(),
            items = orderItems
        )

        orderRepository.placeOrder(order) { success, error ->
            if (success) {
                generateAndUploadReceipt(order, currentUser.uid)
            } else {
                setLoading(false)
                Toast.makeText(this, "Failed to place order: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateAndUploadReceipt(order: Order, userId: String) {
        val qrContent = "OrderID: ${order.orderId}\nUserID: $userId\nTime: ${order.createdAt}"
        val qrBitmap = QrGenerator.generateQrCode(qrContent)

        val pdfFile = PdfGenerator.generateOrderInvoice(this, order, qrBitmap)

        // ALWAYS EMPTY CART AFTER ORDER PLACEMENT
        CartManager.clearCart(this)

        if (pdfFile != null) {
            receiptRepository.uploadReceiptAsBase64(order.orderId, userId, pdfFile) { success, _ ->
                setLoading(false)
                if (success) {
                    Toast.makeText(this, "Order Placed & Invoice Generated!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Order Placed successfully!", Toast.LENGTH_SHORT).show()
                }
                navigateToHome()
            }
        } else {
            setLoading(false)
            Toast.makeText(this, "Order Placed successfully!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnPlaceOrder.isEnabled = !isLoading
    }
}
