package com.example.pencatatankeuangan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.pencatatankeuangan.data.ProductRepository
import com.example.pencatatankeuangan.model.ProductItem

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<ProductItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Semua",
    val isLoading: Boolean = false,
    val message: String? = null
) {
    val filteredProducts: List<ProductItem>
        get() = products.filter { product ->
            val matchesCategory =
                selectedCategory == "Semua" ||
                        product.category == selectedCategory

            val matchesSearch =
                product.name.contains(
                    searchQuery,
                    ignoreCase = true
                ) ||
                        product.brand.contains(
                            searchQuery,
                            ignoreCase = true
                        )

            matchesCategory && matchesSearch
        }
}

class ProductViewModel(
    private val repository: ProductRepository =
        ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProductUiState(isLoading = true)
    )

    val uiState: StateFlow<ProductUiState> =
        _uiState.asStateFlow()

    init {
        loadProducts()
    }

    // READ
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )

            try {
                val products = repository.getProducts()

                _uiState.value = _uiState.value.copy(
                    products = products,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Gagal memuat produk"
                )
            }
        }
    }

    // SEARCH
    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    // FILTER
    fun setCategory(category: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category
        )
    }

    // INSERT
    fun addProduct(
        name: String,
        brand: String,
        category: String,
        unit: String,
        stock: Int,
        note: String
    ) {
        viewModelScope.launch {
            try {
                repository.addProduct(
                    ProductItem(
                        id = 0L,
                        name = name,
                        brand = brand,
                        category = category,
                        unit = unit,
                        stock = stock,
                        note = note
                    )
                )

                _uiState.value = _uiState.value.copy(
                    products = repository.getProducts(),
                    message = "Produk berhasil ditambahkan"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Gagal menambahkan produk"
                )
            }
        }
    }

    // UPDATE
    fun updateProduct(product: ProductItem) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)

                _uiState.value = _uiState.value.copy(
                    products = repository.getProducts(),
                    message = "Perubahan berhasil disimpan"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Gagal memperbarui produk"
                )
            }
        }
    }

    // DELETE
    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(id)

                _uiState.value = _uiState.value.copy(
                    products = repository.getProducts(),
                    message = "Produk berhasil dihapus"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Gagal menghapus produk"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null
        )
    }
}
