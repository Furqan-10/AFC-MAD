package com.example.afc_mad.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.databinding.ItemReceiptBinding
import com.example.afc_mad.models.Receipt
import java.text.SimpleDateFormat
import java.util.*

class ReceiptAdapter(
    private var receipts: List<Receipt>,
    private val onAction: (Receipt, String) -> Unit
) : RecyclerView.Adapter<ReceiptAdapter.VH>() {

    class VH(val binding: ItemReceiptBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReceiptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val receipt = receipts[position]
        holder.binding.tvOrderId.text = "Order #${receipt.orderId}"
        
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(receipt.generatedAt))
        holder.binding.tvDate.text = "Date: $date"

        holder.binding.btnDownload.setOnClickListener { onAction(receipt, "DOWNLOAD") }
        holder.binding.btnShare.setOnClickListener { onAction(receipt, "SHARE") }
        holder.itemView.setOnClickListener { onAction(receipt, "VIEW") }
    }

    override fun getItemCount() = receipts.size

    fun updateList(newList: List<Receipt>) {
        receipts = newList
        notifyDataSetChanged()
    }
}
