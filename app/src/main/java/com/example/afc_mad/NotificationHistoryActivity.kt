package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.NotificationAdapter
import com.example.afc_mad.databinding.ActivityNotificationHistoryBinding
import com.example.afc_mad.repository.NotificationRepository

class NotificationHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationHistoryBinding
    private val repository = NotificationRepository()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadNotifications()

        binding.btnClearAll.setOnClickListener {
            repository.clearAll()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()) { notification ->
            // Mark as read
            if (!notification.read) {
                repository.markAsRead(notification.id)
            }
            
            // Goal 9: Navigate to correct screen
            when (notification.type) {
                "order" -> {
                    val intent = Intent(this, OrderDetailActivity::class.java)
                    intent.putExtra("orderId", notification.relatedId)
                    startActivity(intent)
                }
                "admin_order" -> {
                    val intent = Intent(this, AdminHomeActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        binding.pbLoading.visibility = View.VISIBLE
        repository.getNotifications { notifications ->
            binding.pbLoading.visibility = View.GONE
            adapter.updateNotifications(notifications)
            
            if (notifications.isEmpty()) {
                binding.tvEmptyNotifications.visibility = View.VISIBLE
                binding.btnClearAll.visibility = View.GONE
            } else {
                binding.tvEmptyNotifications.visibility = View.GONE
                binding.btnClearAll.visibility = View.VISIBLE
            }
        }
    }
}
