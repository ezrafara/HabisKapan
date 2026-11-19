package com.example.pencatatankeuangan.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pencatatankeuangan.data.PurchaseRepository
import com.example.pencatatankeuangan.model.Pembelian
import com.example.pencatatankeuangan.model.PembelianRequest
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val repository: PurchaseRepository = PurchaseRepository()
) : ViewModel() {

    // State daftar pembelian
    var daftar by mutableStateOf<List<Pembelian>>(emptyList())
        private set

    // State loading saat mengambil data
    var isLoading by mutableStateOf(false)
        private set

    // Pesan error saat mengambil daftar
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // State saat menyimpan, mengubah, atau menghapus data
    var isSaving by mutableStateOf(false)
        private set

    // Pesan error saat melakukan aksi
    var aksiError by mutableStateOf<String?>(null)
        private set

    init {
        muatDaftar()
    }

    // READ: mengambil semua data pembelian
    fun muatDaftar() {
        viewModelScope.launch {
            refresh()
        }
    }

    private suspend fun refresh() {
        isLoading = true
        errorMessage = null

        try {
            daftar = repository.getAll()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Gagal mengambil data pembelian."
        } finally {
            isLoading = false
        }
    }

    // READ: mengambil detail pembelian berdasarkan ID
    suspend fun ambilDetail(id: Long): Pembelian? {
        return try {
            repository.getById(id)
        } catch (e: Exception) {
            null
        }
    }

    // CREATE: menambahkan pembelian
    fun tambah(
        req: PembelianRequest,
        onSukses: () -> Unit
    ) {
        viewModelScope.launch {
            isSaving = true
            aksiError = null

            try {
                repository.add(req)
                refresh()
                onSukses()
            } catch (e: Exception) {
                aksiError = e.message ?: "Gagal menambahkan pembelian."
            } finally {
                isSaving = false
            }
        }
    }

    // UPDATE: mengubah pembelian
    fun ubah(
        id: Long,
        req: PembelianRequest,
        onSukses: () -> Unit
    ) {
        viewModelScope.launch {
            isSaving = true
            aksiError = null

            try {
                repository.update(id, req)
                refresh()
                onSukses()
            } catch (e: Exception) {
                aksiError = e.message ?: "Gagal mengubah pembelian."
            } finally {
                isSaving = false
            }
        }
    }

    // DELETE: menghapus pembelian
    fun hapus(
        id: Long,
        onSukses: () -> Unit
    ) {
        viewModelScope.launch {
            isSaving = true
            aksiError = null

            try {
                repository.delete(id)
                refresh()
                onSukses()
            } catch (e: Exception) {
                aksiError = e.message ?: "Gagal menghapus pembelian."
            } finally {
                isSaving = false
            }
        }
    }

    // Membersihkan pesan error
    fun bersihkanAksiError() {
        aksiError = null
    }
}
