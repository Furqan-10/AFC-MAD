package com.example.afc_mad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.ReceiptAdapter
import com.example.afc_mad.databinding.ActivityReceiptHistoryBinding
import com.example.afc_mad.models.Receipt
import com.example.afc_mad.repository.ReceiptRepository
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

class ReceiptHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReceiptHistoryBinding
    private val repository = ReceiptRepository()
    private var allReceipts = listOf<Receipt>()
    private lateinit var adapter: ReceiptAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadReceipts()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = ReceiptAdapter(emptyList()) { receipt, action ->
            when (action) {
                "DOWNLOAD" -> downloadReceipt(receipt)
                "SHARE" -> shareReceipt(receipt)
                "VIEW" -> viewReceipt(receipt)
            }
        }
        binding.rvReceipts.layoutManager = LinearLayoutManager(this)
        binding.rvReceipts.adapter = adapter
    }

    private fun loadReceipts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.pbLoading.visibility = View.VISIBLE
        
        repository.getReceiptsForUser(userId) { receipts ->
            binding.pbLoading.visibility = View.GONE
            allReceipts = receipts
            adapter.updateList(receipts)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = allReceipts.filter { it.orderId.lowercase().contains(query) }
                adapter.updateList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getFileFromBase64(receipt: Receipt): File? {
        return try {
            val pdfBytes = Base64.decode(receipt.pdfBase64, Base64.DEFAULT)
            val directory = File(cacheDir, "receipts")
            if (!directory.exists()) directory.mkdirs()
            val file = File(directory, "${receipt.orderId}.pdf")
            FileOutputStream(file).use { it.write(pdfBytes) }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun downloadReceipt(receipt: Receipt) {
        try {
            val pdfBytes = Base64.decode(receipt.pdfBase64, Base64.DEFAULT)
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadDir, "AFC_Invoice_${receipt.orderId}.pdf")
            FileOutputStream(file).use { it.write(pdfBytes) }
            Toast.makeText(this, "Invoice saved to Downloads", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareReceipt(receipt: Receipt) {
        val file = getFileFromBase64(receipt) ?: return
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share Invoice via"))
    }

    private fun viewReceipt(receipt: Receipt) {
        val file = getFileFromBase64(receipt) ?: return
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
}
