package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.FinanceTransaction
import com.example.pencatatankeuangan.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject

// Transaksi keuangan disimpan di tabel Supabase "finance_transactions".
// Saldo awal = satu baris bertipe INITIAL_BALANCE.
class FinanceRepository {

    // =================================
    // MENAMPILKAN SEMUA TRANSAKSI
    // =================================

    suspend fun getTransactions(): List<FinanceTransaction> {
        val userId = SupabaseRest.requireUserId()

        val text = SupabaseRest.request(
            method = "GET",
            path = "/rest/v1/finance_transactions?select=*&user_id=eq.$userId" +
                    "&order=trx_date.desc,id.desc"
        )

        val array = JSONArray(text)

        return List(array.length()) { index ->
            toTransaction(array.getJSONObject(index))
        }
    }

    // =================================
    // MENGAMBIL SALDO AWAL
    // =================================

    suspend fun getOpeningBalance(): Long {
        val userId = SupabaseRest.requireUserId()

        val text = SupabaseRest.request(
            method = "GET",
            path = "/rest/v1/finance_transactions?select=amount" +
                    "&user_id=eq.$userId&type=eq.INITIAL_BALANCE" +
                    "&order=id.desc&limit=1"
        )

        val array = JSONArray(text)

        return if (array.length() == 0) 0L
        else array.getJSONObject(0).optLong("amount", 0L)
    }

    // =================================
    // MENAMBAHKAN TRANSAKSI
    // =================================

    suspend fun addTransaction(transaction: FinanceTransaction) {
        val userId = SupabaseRest.requireUserId()

        // Hanya boleh ada satu saldo awal
        if (transaction.type == TransactionType.INITIAL_BALANCE) {
            deleteInitialBalances(userId, exceptId = null)
        }

        SupabaseRest.request(
            method = "POST",
            path = "/rest/v1/finance_transactions",
            body = toJson(transaction).put("user_id", userId)
        )
    }

    // =================================
    // MENGUBAH TRANSAKSI
    // =================================

    suspend fun updateTransaction(transaction: FinanceTransaction) {
        val userId = SupabaseRest.requireUserId()

        if (transaction.type == TransactionType.INITIAL_BALANCE) {
            deleteInitialBalances(userId, exceptId = transaction.id)
        }

        SupabaseRest.request(
            method = "PATCH",
            path = "/rest/v1/finance_transactions" +
                    "?id=eq.${transaction.id}&user_id=eq.$userId",
            body = toJson(transaction)
        )
    }

    // =================================
    // MENGHAPUS TRANSAKSI
    // =================================

    suspend fun deleteTransaction(id: Long) {
        val userId = SupabaseRest.requireUserId()

        SupabaseRest.request(
            method = "DELETE",
            path = "/rest/v1/finance_transactions?id=eq.$id&user_id=eq.$userId"
        )
    }

    private suspend fun deleteInitialBalances(
        userId: String,
        exceptId: Long?
    ) {
        val except = if (exceptId != null) "&id=neq.$exceptId" else ""

        SupabaseRest.request(
            method = "DELETE",
            path = "/rest/v1/finance_transactions" +
                    "?user_id=eq.$userId&type=eq.INITIAL_BALANCE$except"
        )
    }

    private fun toJson(t: FinanceTransaction) = JSONObject().apply {
        put("title", t.title)
        put("type", t.type.name)
        put("category", t.category)
        put("amount", t.amount)
        put("trx_date", t.date)
        put("note", t.note)
    }

    private fun toTransaction(item: JSONObject) = FinanceTransaction(
        id = item.getLong("id"),
        title = item.getString("title"),
        type = TransactionType.valueOf(item.getString("type")),
        category = item.optString("category", ""),
        amount = item.optLong("amount", 0L),
        date = item.optString("trx_date", "").take(10),
        note = if (item.isNull("note")) "" else item.optString("note")
    )
}
