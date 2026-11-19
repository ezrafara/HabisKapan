package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.ActiveProduct
import com.example.pencatatankeuangan.model.UsageLog
import com.example.pencatatankeuangan.model.usageToday
import kotlinx.coroutines.delay

// Penyimpanan sementara di memori. Dipakai selama Supabase belum dikonfigurasi.
class InMemoryUsageRepository : UsageRepository {

    private val activeProducts = mutableListOf(
        ActiveProduct(1L, 1L, "Sabun Mandi Lifebuoy", 120, "ml", usageToday()),
        ActiveProduct(2L, 2L, "Pasta Gigi Pepsodent", 42, "gram", usageToday()),
        ActiveProduct(3L, 5L, "Sabun Cuci Piring", 25, "ml", usageToday())
    )

    // Data contoh sesuai desain. Ganti dengan mutableListOf() bila ingin kosong.
    private val logs = mutableListOf(
        UsageLog(1L, 1L, 8, "ml", usageToday(), "07:10", "Mandi pagi"),
        UsageLog(2L, 2L, 2, "gram", usageToday(), "07:15", "Sikat gigi pagi"),
        UsageLog(3L, 3L, 12, "ml", usageToday(), "08:05", "Mencuci piring")
    )

    private var nextActiveId = 4L
    private var nextLogId = 4L

    override suspend fun getActiveProducts(): List<ActiveProduct> {
        delay(150)
        return activeProducts.toList()
    }

    override suspend fun addActiveProduct(product: ActiveProduct) {
        delay(150)
        activeProducts.add(product.copy(id = nextActiveId++))
    }

    override suspend fun deleteActiveProduct(id: Long) {
        delay(150)
        activeProducts.removeAll { it.id == id }
        logs.removeAll { it.activeProductId == id }   // sama seperti ON DELETE CASCADE
    }

    override suspend fun getUsageLogs(): List<UsageLog> {
        delay(150)
        return logs.toList()
    }

    override suspend fun addUsageLog(log: UsageLog) {
        delay(150)
        logs.add(log.copy(id = nextLogId++))
    }

    override suspend fun updateUsageLog(log: UsageLog) {
        delay(150)
        val index = logs.indexOfFirst { it.id == log.id }
        if (index >= 0) logs[index] = log
    }

    override suspend fun deleteUsageLog(id: Long) {
        delay(150)
        logs.removeAll { it.id == id }
    }
}
