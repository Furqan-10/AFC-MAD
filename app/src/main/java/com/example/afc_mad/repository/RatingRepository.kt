package com.example.afc_mad.repository

import com.example.afc_mad.models.Product
import com.example.afc_mad.models.Rating
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RatingRepository {
    private val database = FirebaseDatabase.getInstance()
    private val ratingsRef = database.getReference("ratings")
    private val productsRef = database.getReference("products")

    fun submitReview(rating: Rating, onComplete: (Boolean) -> Unit) {
        val ratingId = ratingsRef.push().key ?: return
        val finalRating = rating.copy(ratingId = ratingId, timestamp = System.currentTimeMillis())

        ratingsRef.child(ratingId).setValue(finalRating).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateProductRating(rating.productId, rating.stars, onComplete)
            } else {
                onComplete(false)
            }
        }
    }

    private fun updateProductRating(productId: String, newStars: Int, onComplete: (Boolean) -> Unit) {
        productsRef.child(productId).runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val p = currentData.getValue(Product::class.java) ?: return com.google.firebase.database.Transaction.success(currentData)

                val newTotalRatings = p.totalRatings + 1
                val newAverageRating = ((p.averageRating * p.totalRatings) + newStars) / newTotalRatings

                p.totalRatings = newTotalRatings
                p.averageRating = newAverageRating

                currentData.value = p
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                onComplete(committed)
            }
        })
    }

    fun checkUserReviewStatus(userId: String, orderId: String, productId: String, onResult: (Boolean) -> Unit) {
        ratingsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var reviewed = false
                for (ds in snapshot.children) {
                    val r = ds.getValue(Rating::class.java)
                    if (r?.orderId == orderId && r.productId == productId) {
                        reviewed = true
                        break
                    }
                }
                onResult(reviewed)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(false)
            }
        })
    }

    fun getProductReviews(productId: String, onResult: (List<Rating>) -> Unit) {
        ratingsRef.orderByChild("productId").equalTo(productId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Rating>()
                for (ds in snapshot.children) {
                    ds.getValue(Rating::class.java)?.let { list.add(it) }
                }
                onResult(list.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    fun getAllReviews(onResult: (List<Rating>) -> Unit) {
        ratingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Rating>()
                for (ds in snapshot.children) {
                    ds.getValue(Rating::class.java)?.let { list.add(it) }
                }
                onResult(list.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }
}
