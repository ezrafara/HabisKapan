package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.FinanceTransaction
import com.example.pencatatankeuangan.model.TransactionType
import kotlinx.coroutines.delay

class FinanceRepository {

    private val transactions = mutableListOf(

        FinanceTransaction(
            id = 1L,
            title = "Belanja bulanan",
            type = TransactionType.EXPENSE,
            category = "Kebutuhan Rumah",
            amount = 286500L,
            date = "2026-06-10",
            note = "Belanja kebutuhan bulanan"
        ),

        FinanceTransaction(
            id = 2L,
            title = "Gaji bulanan",
            type = TransactionType.INCOME,
            category = "Gaji",
            amount = 6500000L,
            date = "2026-06-01",
            note = "Gaji bulan Juni"
        ),

        FinanceTransaction(
            id = 3L,
            title = "Perlengkapan mandi",
            type = TransactionType.EXPENSE,
            category = "Perawatan Diri",
            amount = 89700L,
            date = "2026-06-08",
            note = ""
        )
    )

    private var nextId = 4L

    private var openingBalance = 0L

    // =================================
    // MENAMPILKAN SEMUA TRANSAKSI
    // =================================

    suspend fun getTransactions(): List<FinanceTransaction> {

        delay(200)

        return transactions.toList()
    }

    // =================================
    // MENGAMBIL SALDO AWAL
    // =================================

    suspend fun getOpeningBalance(): Long {

        return openingBalance
    }

    // =================================
    // MENAMBAHKAN TRANSAKSI
    // =================================

    suspend fun addTransaction(
        transaction: FinanceTransaction
    ) {

        delay(200)

        if (transaction.type == TransactionType.INITIAL_BALANCE) {

            openingBalance = transaction.amount

            transactions.removeAll {
                it.type == TransactionType.INITIAL_BALANCE
            }
        }

        transactions.add(
            transaction.copy(
                id = nextId++
            )
        )
    }

    // =================================
    // MENGUBAH TRANSAKSI
    // =================================

    suspend fun updateTransaction(
        transaction: FinanceTransaction
    ) {

        delay(200)

        val index = transactions.indexOfFirst {
            it.id == transaction.id
        }

        if (index != -1) {

            val oldTransaction = transactions[index]

            if (
                oldTransaction.type == TransactionType.INITIAL_BALANCE ||
                transaction.type == TransactionType.INITIAL_BALANCE
            ) {

                openingBalance =
                    if (
                        transaction.type ==
                        TransactionType.INITIAL_BALANCE
                    ) {
                        transaction.amount
                    } else {
                        0L
                    }
            }

            transactions[index] = transaction
        }
    }

    // =================================
    // MENGHAPUS TRANSAKSI
    // =================================

    suspend fun deleteTransaction(id: Long) {

        delay(200)

        val transaction = transactions.firstOrNull {
            it.id == id
        }

        if (transaction?.type == TransactionType.INITIAL_BALANCE) {

            openingBalance = 0L
        }

        transactions.removeAll {
            it.id == id
        }
    }
}