package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.ProductItem
import kotlinx.coroutines.delay

class ProductRepository {

    private val products = mutableListOf(
        ProductItem(
            id = 1L,
            name = "Sabun Mandi Lifebuoy",
            brand = "Lifebuoy",
            category = "Perawatan Diri",
            unit = "ml",
            stock = 120,
            note = "Aroma lemon segar"
        ),
        ProductItem(
            id = 2L,
            name = "Pasta Gigi Pepsodent",
            brand = "Pepsodent",
            category = "Perawatan Diri",
            unit = "gram",
            stock = 42
        ),
        ProductItem(
            id = 3L,
            name = "Sampo Pantene",
            brand = "Pantene",
            category = "Perawatan Diri",
            unit = "ml",
            stock = 210
        ),
        ProductItem(
            id = 4L,
            name = "Deterjen Rinso",
            brand = "Rinso",
            category = "Kebersihan",
            unit = "gram",
            stock = 180
        ),
        ProductItem(
            id = 5L,
            name = "Sabun Cuci Piring",
            brand = "Sunlight",
            category = "Dapur",
            unit = "ml",
            stock = 25
        ),
        ProductItem(
            id = 6L,
            name = "Tisu Gulung",
            brand = "Nice",
            category = "Kebersihan",
            unit = "roll",
            stock = 12
        )
    )

    private var nextId = 7L

    // READ
    suspend fun getProducts(): List<ProductItem> {
        delay(200)
        return products.toList()
    }

    // INSERT
    suspend fun addProduct(product: ProductItem) {
        delay(200)
        products.add(product.copy(id = nextId++))
    }

    // UPDATE
    suspend fun updateProduct(product: ProductItem) {
        delay(200)

        val index = products.indexOfFirst {
            it.id == product.id
        }

        if (index >= 0) {
            products[index] = product
        }
    }

    // DELETE
    suspend fun deleteProduct(id: Long) {
        delay(200)
        products.removeAll { it.id == id }
    }
}
