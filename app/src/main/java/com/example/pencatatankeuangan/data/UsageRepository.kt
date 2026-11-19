package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.ActiveProduct
import com.example.pencatatankeuangan.model.UsageLog

// Kontrak data fitur Pemakaian Harian.
// Implementasi: InMemoryUsageRepository (lokal) dan SupabaseUsageRepository.
interface UsageRepository {

    // ---- active_products ----
    suspend fun getActiveProducts(): List<ActiveProduct>
    suspend fun addActiveProduct(product: ActiveProduct)
    suspend fun deleteActiveProduct(id: Long)

    // ---- usage_logs ----
    suspend fun getUsageLogs(): List<UsageLog>      // READ
    suspend fun addUsageLog(log: UsageLog)          // INSERT
    suspend fun updateUsageLog(log: UsageLog)       // UPDATE
    suspend fun deleteUsageLog(id: Long)            // DELETE
}
