package com.example.pencatatankeuangan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pencatatankeuangan.data.ShoppingRepository
import com.example.pencatatankeuangan.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShoppingUiState(
    val items: List<ShoppingItem> = emptyList(),
    val selectedFilter: String = "Semua",
    val isLoading: Boolean = false,
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

class ShoppingViewModel(
    private val repository: ShoppingRepository = ShoppingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingUiState(isLoading = true))

    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    // READ
    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                _uiState.value = _uiState.value.copy(
                    items = repository.getAll(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Gagal memuat daftar belanja: ${e.message.orEmpty()}"
                )
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    // Menjalankan aksi ke Supabase, lalu memuat ulang daftar.
    private fun mutate(successMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()

                _uiState.value = _uiState.value.copy(
                    items = repository.getAll(),
                    message = successMessage
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Gagal menyimpan: ${e.message.orEmpty()}"
                )
            }
        }
    }

    // CREATE
    fun addItem(
        productName: String,
        quantity: Int,
        priority: String,
        note: String
    ) {
        mutate("Item berhasil ditambahkan") {
            repository.add(
                ShoppingItem(
                    id = 0L,
                    productName = productName.trim(),
                    quantity = quantity,
                    priority = priority,
                    isPurchased = false,
                    note = note.trim()
                )
            )
        }
    }

    // Produk dari katalog yang sudah ada tidak ditambahkan berulang.
    // Jika nama produk sudah ada (dan belum dibeli), jumlahnya ditambah.
    fun addItemFromProduct(
        productName: String,
        quantity: Int = 1,
        priority: String = "Sedang",
        note: String = ""
    ) {
        val existingItem = _uiState.value.items.firstOrNull {
            it.productName.trim().equals(productName.trim(), ignoreCase = true) &&
                    !it.isPurchased
        }

        if (existingItem != null) {
            mutate("$productName sudah ada. Jumlah diperbarui.") {
                repository.update(
                    existingItem.copy(quantity = existingItem.quantity + quantity)
                )
            }
        } else {
            addItem(
                productName = productName,
                quantity = quantity,
                priority = priority,
                note = note
            )
        }
    }

    // UPDATE
    fun updateItem(item: ShoppingItem) {
        mutate("Item berhasil diperbarui") {
            repository.update(
                item.copy(
                    productName = item.productName.trim(),
                    note = item.note.trim()
                )
            )
        }
    }

    fun togglePurchased(id: Long) {
        val item = _uiState.value.items.firstOrNull { it.id == id } ?: return

        mutate("Status pembelian berhasil diperbarui") {
            repository.update(item.copy(isPurchased = !item.isPurchased))
        }
    }

    // DELETE
    fun deleteItem(id: Long) {
        mutate("Item berhasil dihapus") {
            repository.delete(id)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
