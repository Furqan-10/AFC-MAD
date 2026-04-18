package com.example.afc_mad

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.models.Banner
import com.example.afc_mad.utils.FileHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class ManageBannersActivity : AppCompatActivity() {

    private lateinit var fileHandler: FileHandler
    private lateinit var rvBanners: RecyclerView
    private lateinit var bannerAdapter: AdminBannerAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            val path = saveImageToInternalStorage(uri)
            if (path != null) {
                val banner = Banner(UUID.randomUUID().toString(), path)
                fileHandler.saveBanner(banner)
                loadBanners()
                Toast.makeText(this, "Banner Added", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_banners)

        fileHandler = FileHandler(this)
        rvBanners = findViewById(R.id.rvBanners)
        
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }

        findViewById<View>(R.id.cardAddBanner).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        setupRecyclerView()
        loadBanners()
    }

    private fun setupRecyclerView() {
        bannerAdapter = AdminBannerAdapter(mutableListOf()) { bannerId ->
            fileHandler.deleteBanner(bannerId)
            loadBanners()
        }
        rvBanners.layoutManager = GridLayoutManager(this, 2)
        rvBanners.adapter = bannerAdapter
    }

    private fun loadBanners() {
        bannerAdapter.updateList(fileHandler.getBanners())
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val fileName = "banner_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: IOException) {
            null
        }
    }

    inner class AdminBannerAdapter(
        private var list: List<Banner>,
        private val onDelete: (String) -> Unit
    ) : RecyclerView.Adapter<AdminBannerAdapter.VH>() {

        fun updateList(newList: List<Banner>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_banner, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.ivImage.setImageURI(Uri.fromFile(File(item.imagePath)))
            holder.btnDelete.setOnClickListener { onDelete(item.id) }
        }

        override fun getItemCount() = list.size

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivAdminBanner)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteBanner)
        }
    }
}
