package com.example.pencatatankeuangan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.pencatatankeuangan.data.FinanceRepository
import com.example.pencatatankeuangan.model.FinanceTransaction
import com.example.pencatatankeuangan.model.TransactionType

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ======================================
// STATE KEUANGAN
// ======================================

data class FinanceUiState(

    val transactions: List<FinanceTransaction> = emptyList(),

    val openingBalance: Long = 0L,

    val isLoading: Boolean = false,

    val message: String? = null

) {

    // TOTAL PEMASUKAN

    val totalIncome: Long
        get() = transactions
            .filter {
                it.type == TransactionType.INCOME
            }
            .sumOf {
                it.amount
            }

    // TOTAL PENGELUARAN

    val totalExpense: Long
        get() = transactions
            .filter {
                it.type == TransactionType.EXPENSE
            }
            .sumOf {
                it.amount
            }

    // SALDO SAAT INI

    val currentBalance: Long
        get() = openingBalance + totalIncome - totalExpense
}

// ======================================
// VIEWMODEL
// ======================================

class FinanceViewModel(
    private val repository: FinanceRepository =
        FinanceRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            FinanceUiState(
                isLoading = true
            )
        )

    val uiState: StateFlow<FinanceUiState> =
        _uiState.asStateFlow()

    init {

        loadData()
    }

    // ==================================
    // MEMUAT DATA
    // ==================================

    fun loadData() {

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true
                )

            try {

                refreshData()

            } catch (e: Exception) {

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        message = "Gagal memuat data"
                    )
            }
        }
    }

    // ==================================
    // REFRESH DATA
    // ==================================

    private suspend fun refreshData() {

        val transactions =
            repository.getTransactions()

        val openingBalance =
            repository.getOpeningBalance()

        _uiState.value =
            _uiState.value.copy(

                transactions =
                    transactions.sortedByDescending {
                        it.date
                    },

                openingBalance = openingBalance,

                isLoading = false
            )
    }

    // ==================================
    // MENAMBAHKAN TRANSAKSI
    // ==================================

    fun addTransaction(

        title: String,

        type: TransactionType,

        category: String,

        amount: Long,

        date: String,

        note: String

    ) {

        viewModelScope.launch {

            try {

                repository.addTransaction(

                    FinanceTransaction(

                        id = 0L,

                        title = title,

                        type = type,

                        category = category,

                        amount = amount,

                        date = date,

                        note = note
                    )
                )

                refreshData()

                _uiState.value =
                    _uiState.value.copy(

                        message =
                            if (
                                type ==
                                TransactionType.INITIAL_BALANCE
                            ) {
                                "Saldo awal berhasil disimpan"
                            } else {
                                "Data baru berhasil ditambahkan"
                            }
                    )

            } catch (e: Exception) {

                _uiState.value =
                    _uiState.value.copy(
                        message = "Gagal menyimpan data"
                    )
            }
        }
    }

    // ==================================
    // MENGEDIT TRANSAKSI
    // ==================================

    fun updateTransaction(
        transaction: FinanceTransaction
    ) {

        viewModelScope.launch {

            try {

                repository.updateTransaction(
                    transaction
                )

                refreshData()

                _uiState.value =
                    _uiState.value.copy(
                        message = "Perubahan berhasil disimpan"
                    )

            } catch (e: Exception) {

                _uiState.value =
                    _uiState.value.copy(
                        message = "Gagal memperbarui data"
                    )
            }
        }
    }

    // ==================================
    // MENGHAPUS TRANSAKSI
    // ==================================

    fun deleteTransaction(id: Long) {

        viewModelScope.launch {

            try {

                repository.deleteTransaction(id)

                refreshData()

                _uiState.value =
                    _uiState.value.copy(
                        message = "Data berhasil dihapus"
                    )

            } catch (e: Exception) {

                _uiState.value =
                    _uiState.value.copy(
                        message = "Gagal menghapus data"
                    )
            }
        }
    }

    // ==================================
    // MENGHAPUS NOTIFIKASI
    // ==================================

    fun clearMessage() {

        _uiState.value =
            _uiState.value.copy(
                message = null
            )
    }
}