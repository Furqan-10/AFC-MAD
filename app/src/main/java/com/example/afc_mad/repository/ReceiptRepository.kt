package com.example.afc_mad.repository

import android.util.Base64
import com.example.afc_mad.models.Receipt
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File

class ReceiptRepository {
    private val database = FirebaseDatabase.getInstance().getReference("receipts")

    fun uploadReceiptAsBase64(orderId: String, userId: String, pdfFile: File, onComplete: (Boolean, String?) -> Unit) {
        try {
            val bytes = pdfFile.readBytes()
            val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
            
            val receipt = Receipt(orderId, userId, base64String, System.currentTimeMillis())
            database.child(orderId).setValue(receipt)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful, if (task.isSuccessful) null else task.exception?.message)
                }
        } catch (e: Exception) {
            onComplete(false, e.message)
        }
    }

    fun getReceiptsForUser(userId: String, onUpdate: (List<Receipt>) -> Unit) {
        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Receipt>()
                    for (child in snapshot.children) {
                        child.getValue(Receipt::class.java)?.let { list.add(it) }
                    }
                    onUpdate(list.sortedByDescending { it.generatedAt })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
