package com.example.afc_mad.models

import java.io.Serializable

data class Product(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var price: Int = 0,
    var categoryId: String = "",
    var imageUrl: String = "",
    var available: Boolean = true,
    var averageRating: Double = 0.0,
    var totalRatings: Int = 0
) : Serializable
