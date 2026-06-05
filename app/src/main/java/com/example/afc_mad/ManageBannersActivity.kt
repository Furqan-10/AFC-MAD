package com.example.afc_mad

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import com.bumptech.glide.Glide
import com.example.afc_mad.models.Banner
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.util.UUID

class ManageBannersActivity : AppCompatActivity() {

    private lateinit var rvBanners: RecyclerView
    private lateinit var bannerAdapter: AdminBannerAdapter
    
    private val database = FirebaseDatabase.getInstance().getReference("banners")

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            uploadBanner(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_banners)

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
        loadBannersFromFirebase()
    }

    private fun setupRecyclerView() {
        bannerAdapter = AdminBannerAdapter(mutableListOf()) { banner ->
            database.child(banner.id).removeValue()
        }
        rvBanners.layoutManager = GridLayoutManager(this, 2)
        rvBanners.adapter = bannerAdapter
    }

    private fun loadBannersFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Banner>()
                for (child in snapshot.children) {
                    val b = child.getValue(Banner::class.java)
                    if (b != null) list.add(b)
                }
                bannerAdapter.updateList(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun uploadBanner(uri: Uri) {
        val base64Image = uriToBase64(uri)
        if (base64Image != null) {
            val id = database.push().key ?: UUID.randomUUID().toString()
            val banner = Banner(id, base64Image)
            database.child(id).setValue(banner).addOnSuccessListener {
                Toast.makeText(this, "Banner Added", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    inner class AdminBannerAdapter(
        private var list: List<Banner>,
        private val onDelete: (Banner) -> Unit
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
            val imageBytes = Base64.decode(item.imagePath, Base64.DEFAULT)
            Glide.with(holder.ivImage.context).asBitmap().load(imageBytes).into(holder.ivImage)
            holder.btnDelete.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount() = list.size

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivAdminBanner)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteBanner)
        }
    }
}
