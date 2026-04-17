package com.example.afc_mad

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.models.MenuItem
import com.example.afc_mad.utils.FileHandler

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize sample data
        val fileHandler = FileHandler(this)
        if (fileHandler.getMenuItems().isEmpty()) {
            val sampleItems = listOf(
                MenuItem(
                    "1", 
                    "Krunch Burger", 
                    310.0, 
                    "Krunch fillet, spicy mayo, lettuce, sandwiched between a sesame seed bun", 
                    "Burgers",
                    "krunch_burger"
                ),
                MenuItem(
                    "2", 
                    "Mighty Zinger", 
                    770.0, 
                    "2 pieces of Zinger fillets with spicy mayo, lettuce and cheese", 
                    "Zinger",
                    "mighty_zinger"
                ),
                MenuItem(
                    "3", 
                    "Family Bucket", 
                    1850.0, 
                    "9 pieces of Hot and Crispy chicken with 2 large fries and 1.5L drink", 
                    "Deals",
                    "family_bucket"
                )
            )
            sampleItems.forEach { fileHandler.saveMenuItem(it) }
        }

        // Redirect to Splash
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}