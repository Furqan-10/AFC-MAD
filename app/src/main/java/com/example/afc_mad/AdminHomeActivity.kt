package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivityAdminHomeBinding

class AdminHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnManageMenu.setOnClickListener {
            startActivity(Intent(this, ManageMenuActivity::class.java))
        }

        binding.btnManageCategories.setOnClickListener {
            startActivity(Intent(this, ManageCategoriesActivity::class.java))
        }

        binding.btnViewOrders.setOnClickListener {
            startActivity(Intent(this, ViewOrdersActivity::class.java))
        }
        
        binding.btnManageBanners.setOnClickListener {
            startActivity(Intent(this, ManageBannersActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
