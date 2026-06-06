package com.example.afc_mad

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afc_mad.adapters.ReviewAdapter
import com.example.afc_mad.databinding.ActivityReviewDashboardBinding
import com.example.afc_mad.models.Product
import com.example.afc_mad.models.Rating
import com.example.afc_mad.repository.RatingRepository
import com.google.firebase.database.FirebaseDatabase

class ReviewDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewDashboardBinding
    private val ratingRepository = RatingRepository()
    private lateinit var adapter: ReviewAdapter
    private var allReviews: List<Rating> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupSearch()
        fetchReviews()
        fetchProductInsights()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(emptyList())
        binding.rvAllReviews.layoutManager = LinearLayoutManager(this)
        binding.rvAllReviews.adapter = adapter
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener { filterReviews(0) }
        binding.chip5Star.setOnClickListener { filterReviews(5) }
        binding.chip4Star.setOnClickListener { filterReviews(4) }
        binding.chip1Star.setOnClickListener { filterReviews(1) }
    }

    private fun filterReviews(stars: Int) {
        val filteredList = if (stars == 0) allReviews else allReviews.filter { it.stars == stars }
        adapter.updateList(filteredList)
    }

    private fun setupSearch() {
        binding.etSearchReviews.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filteredList = allReviews.filter { 
                    it.userName.lowercase().contains(query) || it.review.lowercase().contains(query)
                }
                adapter.updateList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchReviews() {
        ratingRepository.getAllReviews { reviews ->
            allReviews = reviews
            adapter.updateList(reviews)
        }
    }

    private fun fetchProductInsights() {
        FirebaseDatabase.getInstance().getReference("products").get().addOnSuccessListener { snapshot ->
            val products = mutableListOf<Product>()
            for (ds in snapshot.children) {
                ds.getValue(Product::class.java)?.let { products.add(it) }
            }

            if (products.isNotEmpty()) {
                val highest = products.maxByOrNull { it.averageRating }
                val lowest = products.filter { it.totalRatings > 0 }.minByOrNull { it.averageRating }
                val mostReviewed = products.maxByOrNull { it.totalRatings }

                binding.tvHighestRated.text = getString(R.string.highest_rated_format, highest?.name ?: "N/A", highest?.averageRating ?: 0.0)
                binding.tvLowestRated.text = getString(R.string.lowest_rated_format, lowest?.name ?: "N/A", lowest?.averageRating ?: 0.0)
                binding.tvMostReviewed.text = getString(R.string.most_reviewed_format, mostReviewed?.name ?: "N/A", mostReviewed?.totalRatings ?: 0)
            }
        }
    }
}
