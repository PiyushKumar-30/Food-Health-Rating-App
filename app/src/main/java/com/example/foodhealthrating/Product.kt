package com.example.foodhealthrating.model

import android.net.Uri

data class Product(
    val name: String,
    val calories: String,
    val imageUri: Uri? = null, // Supports gallery images
    val imageResId: Int? = null // Supports drawable resources
)
