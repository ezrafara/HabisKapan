package com.example.pencatatankeuangan.model

data class ShoppingItem(
    val id: Long,
    val productName: String,
    val quantity: Int,
    val priority: String,
    val isPurchased: Boolean = false,
    val note: String = ""
)
