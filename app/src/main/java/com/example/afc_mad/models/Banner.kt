package com.example.afc_mad.models

import java.io.Serializable

data class Banner(
    var id: String = "",
    var imagePath: String = "" // This will be the Firebase Storage URL
) : Serializable
