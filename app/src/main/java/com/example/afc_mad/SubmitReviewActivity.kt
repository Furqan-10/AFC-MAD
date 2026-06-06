package com.example.afc_mad

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.afc_mad.databinding.ActivitySubmitReviewBinding
import com.example.afc_mad.models.Rating
import com.example.afc_mad.models.User
import com.example.afc_mad.repository.NotificationRepository
import com.example.afc_mad.repository.RatingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SubmitReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubmitReviewBinding
    private val ratingRepository = RatingRepository()
    private val notificationRepository = NotificationRepository()
    private var productId: String = ""
    private var productName: String = ""
    private var orderId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmitReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        productName = intent.getStringExtra("PRODUCT_NAME") ?: ""
        orderId = intent.getStringExtra("ORDER_ID") ?: ""

        if (productId.isEmpty() || orderId.isEmpty()) {
            Toast.makeText(this, "Invalid Product or Order", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnSubmitReview.setOnClickListener {
            val stars = binding.ratingBar.rating.toInt()
            val reviewText = binding.etReview.text.toString().trim()

            if (stars == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            ratingRepository.checkUserReviewStatus(userId, orderId, productId) { reviewed ->
                if (reviewed) {
                    Toast.makeText(this, "You have already reviewed this product for this order", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    submitReview(stars, reviewText)
                }
            }
        }
    }

    private fun submitReview(stars: Int, reviewText: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        FirebaseDatabase.getInstance().getReference("users").child(userId)
            .get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                val userName = user?.name ?: "Anonymous"

                val rating = Rating(
                    productId = productId,
                    productName = productName,
                    userId = userId,
                    orderId = orderId,
                    userName = userName,
                    stars = stars,
                    review = reviewText
                )

                ratingRepository.submitReview(rating) { success ->
                    if (success) {
                        Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                        notificationRepository.notifyAdmins(
                            "New Review Received",
                            "$userName gave $stars stars for $productName",
                            "review",
                            productId
                        )
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
