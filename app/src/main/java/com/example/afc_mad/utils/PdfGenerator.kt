package com.example.afc_mad.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.afc_mad.models.Order
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateOrderInvoice(context: Context, order: Order, qrBitmap: Bitmap?): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        
        // Page Configuration (A4 size approximation in points: 595 x 842)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // 1. Header (AFC Branding)
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 24f
        titlePaint.color = Color.RED
        canvas.drawText("AFC - Advanced Food Center", 50f, 60f, titlePaint)
        
        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText("Contact: +92-300-1234567", 50f, 85f, paint)
        canvas.drawText("Email: support@afc.com", 50f, 105f, paint)
        
        canvas.drawLine(50f, 120f, 545f, 120f, paint)

        // 2. Customer & Order Info
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INVOICE TO:", 50f, 150f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Name: ${order.customerName}", 50f, 170f, paint)
        canvas.drawText("Phone: ${order.phone}", 50f, 190f, paint)
        canvas.drawText("Address: ${order.address}", 50f, 210f, paint)

        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.createdAt))
        canvas.drawText("Order ID: ${order.orderId}", 350f, 170f, paint)
        canvas.drawText("Date: $date", 350f, 190f, paint)
        canvas.drawText("Payment: ${order.paymentMethod}", 350f, 210f, paint)
        canvas.drawText("Status: ${order.status}", 350f, 230f, paint)

        // 3. Products Table Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawRect(50f, 260f, 545f, 285f, Paint().apply { color = Color.LTGRAY })
        canvas.drawText("Product", 60f, 278f, paint)
        canvas.drawText("Qty", 300f, 278f, paint)
        canvas.drawText("Price", 380f, 278f, paint)
        canvas.drawText("Total", 480f, 278f, paint)

        // 4. Products List
        var yPos = 310f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        order.items.forEach { item ->
            canvas.drawText(item.productName, 60f, yPos, paint)
            canvas.drawText(item.quantity.toString(), 300f, yPos, paint)
            canvas.drawText(item.price.toInt().toString(), 380f, yPos, paint)
            canvas.drawText((item.price * item.quantity).toInt().toString(), 480f, yPos, paint)
            yPos += 25f
        }

        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        
        // 5. Totals
        yPos += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Grand Total:", 380f, yPos, paint)
        canvas.drawText("Rs ${order.totalAmount.toInt()}", 480f, yPos, paint)

        // 6. QR Code
        qrBitmap?.let {
            val scaledQr = Bitmap.createScaledBitmap(it, 100, 100, false)
            canvas.drawBitmap(scaledQr, 50f, yPos + 20f, null)
        }

        // 7. Footer
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Thank you for ordering with AFC!", 50f, 800f, paint)
        canvas.drawText("This is a computer generated invoice.", 50f, 815f, paint)

        pdfDocument.finishPage(page)

        // Save to File
        val directory = File(context.filesDir, "receipts")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, "${order.orderId}.pdf")
        
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
