package com.example.afc_mad.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.afc_mad.R
import com.example.afc_mad.models.Rating
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter(private var reviews: List<Rating>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvReviewerName)
        val tvDate: TextView = view.findViewById(R.id.tvReviewTimestamp)
        val tvText: TextView = view.findViewById(R.id.tvReviewText)
        val ratingBar: RatingBar = view.findViewById(R.id.reviewRatingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.tvName.text = review.userName
        holder.tvText.text = review.review
        holder.ratingBar.rating = review.stars.toFloat()

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(review.timestamp))
        
        if (review.review.isEmpty()) {
            holder.tvText.visibility = View.GONE
        } else {
            holder.tvText.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateList(newList: List<Rating>) {
        reviews = newList
        notifyDataSetChanged()
    }
}
