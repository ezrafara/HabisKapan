package com.example.pencatatankeuangan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pencatatankeuangan.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ShoppingUiState(
    val items: List<ShoppingItem> = emptyList(),
    val selectedFilter: String = "Semua",
    val message: String? = null
) {
    val filteredItems: List<ShoppingItem>
        get() = when (selectedFilter) {
            "Belum Dibeli" -> items.filter { !it.isPurchased }
            "Sudah Dibeli" -> items.filter { it.isPurchased }
            else -> items
        }

    val purchasedCount: Int
        get() = items.count { it.isPurchased }

    val totalCount: Int
        get() = items.size

    val progress: Float
        get() = if (totalCount == 0) 0f
        else purchasedCount.toFloat() / totalCount
}

class ShoppingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ShoppingUiState(
            items = listOf(
                ShoppingItem(1, "Pasta Gigi", 2, "Tinggi", false, "Ukuran besar"),
                ShoppingItem(2, "Sabun Mandi", 3, "Sedang", true, "Aroma lavender"),
                ShoppingItem(3, "Sampo", 1, "Tinggi", false, "Anti ketombe"),
                ShoppingItem(4, "Detergen", 2, "Rendah", true, "Kemasan refill")
            )
        )
    )

    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun addItem(
        productName: String,
        quantity: Int,
        priority: String,
        note: String
    ) {
        val currentItems = _uiState.value.items
        val newItem = ShoppingItem(
            id = (currentItems.maxOfOrNull { it.id } ?: 0) + 1,
            productName = productName.trim(),
            quantity = quantity,
            priority = priority,
            isPurchased = false,
            note = note.trim()
        )

        _uiState.value = _uiState.value.copy(
            items = currentItems + newItem,
            message = "Item berhasil ditambahkan"
        )
    }

    // Produk dari katalog yang sudah ada tidak ditambahkan berulang.
    // Jika nama produk sudah ada, jumlahnya ditambah satu.
    fun addItemFromProduct(
        productName: String,
        quantity: Int = 1,
        priority: String = "Sedang",
        note: String = ""
    ) {
        val currentItems = _uiState.value.items
        val existingItem = currentItems.firstOrNull {
            it.productName.trim().equals(productName.trim(), ignoreCase = true) &&
                    !it.isPurchased
        }

        if (existingItem != null) {
            _uiState.value = _uiState.value.copy(
                items = currentItems.map {
                    if (it.id == existingItem.id) {
                        it.copy(quantity = it.quantity + quantity)
                    } else it
                },
                message = "$productName sudah ada. Jumlah diperbarui."
            )
        } else {
            addItem(
                productName = productName,
                quantity = quantity,
                priority = priority,
                note = note
            )
        }
    }

    fun updateItem(item: ShoppingItem) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.id == item.id) item.copy(
                    productName = item.productName.trim(),
                    note = item.note.trim()
                ) else it
            },
            message = "Item berhasil diperbarui"
        )
    }

    fun togglePurchased(id: Int) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.id == id) it.copy(isPurchased = !it.isPurchased) else it
            },
            message = "Status pembelian berhasil diperbarui"
        )
    }

    fun deleteItem(id: Int) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.filter { it.id != id },
            message = "Item berhasil dihapus"
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}