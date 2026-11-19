package com.example.pencatatankeuangan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pencatatankeuangan.data.InMemoryUsageRepository
import com.example.pencatatankeuangan.data.SupabaseConfig
import com.example.pencatatankeuangan.data.SupabaseUsageRepository
import com.example.pencatatankeuangan.data.UsageRepository
import com.example.pencatatankeuangan.model.ActiveProduct
import com.example.pencatatankeuangan.model.ProductUsageStats
import com.example.pencatatankeuangan.model.UsageEntry
import com.example.pencatatankeuangan.model.UsageLog
import com.example.pencatatankeuangan.model.usageToday
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsageUiState(
    val activeProducts: List<ActiveProduct> = emptyList(),
    val logs: List<UsageLog> = emptyList(),
    val selectedDate: String = usageToday(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val isToday: Boolean
        get() = selectedDate == usageToday()

    // Semua log digabung dengan produk aktifnya
    val entries: List<UsageEntry>
        get() {
            val byId = activeProducts.associateBy { it.id }
            return logs.map { UsageEntry(it, byId[it.activeProductId]) }
        }

    // Aktivitas pada tanggal terpilih, waktu terbaru di atas
    val visibleEntries: List<UsageEntry>
        get() = entries
            .filter { it.log.date == selectedDate }
            .sortedByDescending { it.log.sortKey }

    // Total pemakaian hari terpilih (angka lintas unit)
    val totalQuantity: Int
        get() = visibleEntries.sumOf { it.log.quantity }

    // Frekuensi / jumlah aktivitas hari terpilih
    val activityCount: Int
        get() = visibleEntries.size

    // Akumulasi pemakaian satu produk aktif
    fun statsFor(activeProductId: Long): ProductUsageStats? {
        val product = activeProducts.firstOrNull { it.id == activeProductId }
            ?: return null

        return ProductUsageStats(
            product = product,
            logs = logs
                .filter { it.activeProductId == activeProductId }
                .sortedByDescending { it.sortKey }
        )
    }
}

class UsageViewModel(
    private val repository: UsageRepository =
        if (SupabaseConfig.isConfigured) SupabaseUsageRepository()
        else InMemoryUsageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsageUiState(isLoading = true))
    val uiState: StateFlow<UsageUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    // READ
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val products = repository.getActiveProducts()
                val logs = repository.getUsageLogs()

                _uiState.update {
                    it.copy(
                        activeProducts = products,
                        logs = logs,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat data pemakaian"
                    )
                }
            }
        }
    }

    // Pilih tanggal riwayat
    fun setDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // INSERT
    fun addUsage(log: UsageLog, onDone: () -> Unit = {}) {
        runAction("Gagal menyimpan pemakaian", {
            setDate(log.date)
            onDone()
        }) {
            repository.addUsageLog(log)
        }
    }

    // UPDATE
    fun updateUsage(log: UsageLog, onDone: () -> Unit = {}) {
        runAction("Gagal memperbarui pemakaian", {
            setDate(log.date)
            onDone()
        }) {
            repository.updateUsageLog(log)
        }
    }

    // DELETE
    fun deleteUsage(id: Long, onDone: () -> Unit = {}) {
        runAction("Gagal menghapus pemakaian", onDone) {
            repository.deleteUsageLog(id)
        }
    }

    // INSERT produk aktif
    fun addActiveProduct(product: ActiveProduct, onDone: () -> Unit = {}) {
        runAction("Gagal menyimpan produk aktif", onDone) {
            repository.addActiveProduct(product)
        }
    }

    // DELETE produk aktif
    fun deleteActiveProduct(id: Long) {
        runAction("Gagal menghapus produk aktif", {}) {
            repository.deleteActiveProduct(id)
        }
    }

    // Jalankan aksi tulis lalu muat ulang data
    private fun runAction(
        errorText: String,
        onDone: () -> Unit,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                block()

                val products = repository.getActiveProducts()
                val logs = repository.getUsageLogs()

                _uiState.update {
                    it.copy(
                        activeProducts = products,
                        logs = logs,
                        isSaving = false
                    )
                }

                onDone()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = errorText)
                }
            }
        }
    }
}
