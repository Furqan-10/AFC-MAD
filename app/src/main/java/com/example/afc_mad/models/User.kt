package com.example.afc_mad.models

data class User(
    val phone: String,
    val address: String,
    val pin: String,
    val isAdmin: Boolean = false
)
