package com.example.pencatatankeuangan.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    INITIAL_BALANCE
}

data class FinanceTransaction(
    val id: Long,
    val title: String,
    val type: TransactionType,
    val category: String,
    val amount: Long,
    val date: String,
    val note: String = ""
)