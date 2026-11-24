
package com.example.pencatatankeuangan.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.pencatatankeuangan.data.ProductImageRepository
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
    val isSaving: Boolean = false,
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
    application: Application
) : AndroidViewModel(application) {

    private val repository = ProductRepository()
    private val imageRepository = ProductImageRepository(application)

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

    // INSERT (upload foto dulu jika ada, lalu simpan produk)
    fun addProduct(
        name: String,
        brand: String,
        category: String,
        unit: String,
        stock: Int,
        note: String,
        imageUri: Uri? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val imageUrl = imageUri?.let {
                    imageRepository.uploadImage(it)
                }

                repository.addProduct(
                    ProductItem(
                        id = 0L,
                        name = name,
                        brand = brand,
                        category = category,
                        unit = unit,
                        stock = stock,
                        note = note,
                        imageUrl = imageUrl
                    )
                )

                _uiState.value = _uiState.value.copy(
                    products = repository.getProducts(),
                    isSaving = false,
                    message = "Produk berhasil ditambahkan"
                )
                onResult(true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = "Gagal menambahkan produk: ${e.message.orEmpty()}"
                )
                onResult(false)
            }
        }
    }

    // UPDATE (jika ada foto baru, upload dulu lalu ganti image_url)
    fun updateProduct(
        product: ProductItem,
        newImageUri: Uri? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val finalProduct = if (newImageUri != null) {
                    product.copy(
                        imageUrl = imageRepository.uploadImage(newImageUri)
                    )
                } else {
                    product
                }

                repository.updateProduct(finalProduct)

                _uiState.value = _uiState.value.copy(
                    products = repository.getProducts(),
                    isSaving = false,
                    message = "Perubahan berhasil disimpan"
                )
                onResult(true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    message = "Gagal memperbarui produk: ${e.message.orEmpty()}"
                )
                onResult(false)
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

    // CLEAR MESSAGE
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null
        )
    }
}
