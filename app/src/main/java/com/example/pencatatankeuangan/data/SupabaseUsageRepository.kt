package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.ActiveProduct
import com.example.pencatatankeuangan.model.UsageLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// Akses tabel usage_logs & active_products lewat Supabase REST (PostgREST).
// Data per user dibatasi oleh Row Level Security (lihat supabase_usage_schema.sql).
class SupabaseUsageRepository : UsageRepository {

    // ---------------- active_products ----------------

    override suspend fun getActiveProducts(): List<ActiveProduct> {
        val text = request(
            "GET",
            "active_products?select=*&order=created_at.desc" + userFilter()
        )

        return JSONArray(text).objects().map {
            ActiveProduct(
                id = it.getLong("id"),
                productId = it.optLong("product_id"),
                productName = it.optString("product_name"),
                initialQuantity = it.optInt("initial_quantity"),
                unit = it.optString("unit"),
                startDate = it.optString("start_date").take(10)
            )
        }
    }

    override suspend fun addActiveProduct(product: ActiveProduct) {
        val body = JSONObject()
            .put("product_id", product.productId)
            .put("product_name", product.productName)
            .put("initial_quantity", product.initialQuantity)
            .put("unit", product.unit)
            .put("start_date", product.startDate)
        withUser(body)

        request("POST", "active_products", body)
    }

    override suspend fun deleteActiveProduct(id: Long) {
        request("DELETE", "active_products?id=eq.$id")
    }

    // ---------------- usage_logs ----------------

    // READ: daftar + detail aktivitas milik user yang sedang login
    override suspend fun getUsageLogs(): List<UsageLog> {
        val text = request(
            "GET",
            "usage_logs?select=*&order=usage_date.desc,usage_time.desc" + userFilter()
        )

        return JSONArray(text).objects().map {
            UsageLog(
                id = it.getLong("id"),
                activeProductId = it.getLong("active_product_id"),
                quantity = it.optInt("quantity"),
                unit = it.optString("unit"),
                date = it.optString("usage_date").take(10),
                time = it.optString("usage_time").take(5),
                note = if (it.isNull("note")) "" else it.optString("note")
            )
        }
    }

    // INSERT: terhubung ke active_products lewat active_product_id
    override suspend fun addUsageLog(log: UsageLog) {
        val body = log.toJson()
        withUser(body)

        request("POST", "usage_logs", body)
    }

    // UPDATE: jumlah, tanggal, waktu, catatan
    override suspend fun updateUsageLog(log: UsageLog) {
        request("PATCH", "usage_logs?id=eq.${log.id}", log.toJson())
    }

    // DELETE
    override suspend fun deleteUsageLog(id: Long) {
        request("DELETE", "usage_logs?id=eq.$id")
    }

    // ---------------- util ----------------

    private fun UsageLog.toJson(): JSONObject =
        JSONObject()
            .put("active_product_id", activeProductId)
            .put("quantity", quantity)
            .put("unit", unit)
            .put("usage_date", date)
            .put("usage_time", time)
            .put("note", note)

    private fun JSONArray.objects(): List<JSONObject> =
        (0 until length()).map { getJSONObject(it) }

    private fun userFilter(): String {
        val id = SupabaseConfig.userId
        return if (id.isNullOrBlank()) "" else "&user_id=eq.$id"
    }

    private fun withUser(body: JSONObject) {
        val id = SupabaseConfig.userId
        if (!id.isNullOrBlank()) body.put("user_id", id)
    }

    private suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null
    ): String = withContext(Dispatchers.IO) {

        val connection = URL("${SupabaseConfig.url}/rest/v1/$path")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000

            connection.setRequestProperty("apikey", SupabaseConfig.anonKey)
            connection.setRequestProperty(
                "Authorization",
                "Bearer ${SupabaseConfig.accessToken ?: SupabaseConfig.anonKey}"
            )
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Prefer", "return=minimal")

            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream
            else connection.errorStream

            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (code !in 200..299) {
                throw IOException("Supabase $code: $text")
            }

            text.ifBlank { "[]" }
        } finally {
            connection.disconnect()
        }
    }
}
