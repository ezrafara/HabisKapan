package com.example.pencatatankeuangan.model

data class ProductItem(
    val id: Long,
    val name: String,
    val brand: String,
    val category: String,
    val unit: String,
    val stock: Int,
    val note: String = ""
) {
    val availabilityLabel: String
        get() = if (stock <= 20) "Hampir" else "Tersedia"
}
