package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ActivityOrderDetailBinding
import com.example.afc_mad.models.Order
import com.example.afc_mad.models.OrderItem
import com.example.afc_mad.repository.RatingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailBinding
    private var orderId: String? = null
    private val ratingRepository = RatingRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getStringExtra("orderId")
        
        setupToolbar()
        if (orderId != null) {
            fetchOrderDetails(orderId!!)
        } else {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun fetchOrderDetails(id: String) {
        FirebaseDatabase.getInstance().getReference("orders").child(id)
            .get().addOnSuccessListener { snapshot ->
                val order = snapshot.getValue(Order::class.java)
                if (order != null) {
                    displayOrderDetails(order)
                } else {
                    Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun displayOrderDetails(order: Order) {
        binding.tvDetailOrderId.text = getString(R.string.order_id_format, order.orderId)
        binding.tvDetailStatus.text = getString(R.string.status_format, order.status)
        binding.tvDetailTotal.text = getString(R.string.total_format, order.totalAmount.toInt())
        
        setupItemsRecyclerView(order)
    }

    private fun setupItemsRecyclerView(order: Order) {
        val adapter = OrderItemsDetailAdapter(order)
        binding.rvOrderItems.layoutManager = LinearLayoutManager(this)
        binding.rvOrderItems.adapter = adapter
    }

    inner class OrderItemsDetailAdapter(private val order: Order) : 
        RecyclerView.Adapter<OrderItemsDetailAdapter.VH>() {
        
        private val items = order.items

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.tvCartName)
            val price: TextView = view.findViewById(R.id.tvCartPrice)
            val qty: TextView = view.findViewById(R.id.tvQuantity)
            val btnRate: Button = view.findViewById(R.id.btnRateProduct)
            init {
                view.findViewById<View>(R.id.btnPlus).visibility = View.GONE
                view.findViewById<View>(R.id.btnMinus).visibility = View.GONE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.name.text = item.productName
            holder.qty.text = getString(R.string.qty_format, item.quantity)
            holder.price.text = getString(R.string.price_format, (item.price * item.quantity).toInt())

            if (order.status == "Delivered") {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                ratingRepository.checkUserReviewStatus(userId, order.orderId, item.productId) { reviewed ->
                    if (!reviewed) {
                        holder.btnRate.visibility = View.VISIBLE
                        holder.btnRate.setOnClickListener {
                            val intent = Intent(this@OrderDetailActivity, SubmitReviewActivity::class.java)
                            intent.putExtra("PRODUCT_ID", item.productId)
                            intent.putExtra("PRODUCT_NAME", item.productName)
                            intent.putExtra("ORDER_ID", order.orderId)
                            startActivity(intent)
                        }
                    } else {
                        holder.btnRate.visibility = View.GONE
                    }
                }
            } else {
                holder.btnRate.visibility = View.GONE
            }
        }

        override fun getItemCount() = items.size
    }
}
