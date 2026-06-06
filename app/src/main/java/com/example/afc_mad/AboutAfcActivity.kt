package com.example.afc_mad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivityAboutAfcBinding
import com.example.afc_mad.utils.MapUtils

class AboutAfcActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutAfcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutAfcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnViewLocation.setOnClickListener {
            MapUtils.openBranchLocation(this)
        }

        binding.btnGetDirections.setOnClickListener {
            MapUtils.getDirections(this)
        }
    }
}
