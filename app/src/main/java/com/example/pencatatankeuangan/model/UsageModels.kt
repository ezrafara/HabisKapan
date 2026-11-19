package com.example.pencatatankeuangan.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ======================================================
// TABEL active_products : kemasan produk yang sedang dipakai
// ======================================================
data class ActiveProduct(
    val id: Long,
    val productId: Long,          // id produk di katalog
    val productName: String,
    val initialQuantity: Int,     // jumlah awal kemasan
    val unit: String,
    val startDate: String         // yyyy-MM-dd
)

// ======================================================
// TABEL usage_logs : satu aktivitas pemakaian
// ======================================================
data class UsageLog(
    val id: Long,
    val activeProductId: Long,
    val quantity: Int,
    val unit: String,
    val date: String,             // yyyy-MM-dd
    val time: String,             // HH:mm
    val note: String = ""
) {
    val sortKey: String
        get() = "$date $time"

    val quantityLabel: String
        get() = "−$quantity $unit"
}

// Gabungan log + produk aktif (untuk ditampilkan di list)
data class UsageEntry(
    val log: UsageLog,
    val product: ActiveProduct?
) {
    val productName: String
        get() = product?.productName ?: "Produk tidak ditemukan"
}

// Akumulasi pemakaian satu produk aktif
data class ProductUsageStats(
    val product: ActiveProduct,
    val logs: List<UsageLog>      // terbaru lebih dulu
) {
    // Frekuensi penggunaan
    val frequency: Int
        get() = logs.size

    // Total terpakai (hanya satuan yang sama dengan kemasan)
    val used: Int
        get() = logs.filter { it.unit == product.unit }.sumOf { it.quantity }

    // Estimasi sisa produk
    val remaining: Int
        get() = (product.initialQuantity - used).coerceAtLeast(0)

    val remainingFraction: Float
        get() = if (product.initialQuantity <= 0) 0f
        else (remaining.toFloat() / product.initialQuantity).coerceIn(0f, 1f)
}

// ======================================================
// FORMAT TANGGAL & WAKTU
// ======================================================
private val usageLocale = Locale("id", "ID")

fun usageToday(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

fun usageNowTime(): String =
    SimpleDateFormat("HH:mm", Locale.US).format(Date())

// yyyy-MM-dd -> "18 November 2026" / "Rabu, 18 November 2026"
fun usageDisplayDate(iso: String, withDay: Boolean = false): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat(
            if (withDay) "EEEE, d MMMM yyyy" else "d MMMM yyyy",
            usageLocale
        )
        output.format(input.parse(iso)!!)
    } catch (e: Exception) {
        iso
    }
}

// DatePicker Material3 memakai milidetik UTC
fun usageIsoToMillis(iso: String): Long {
    return try {
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        f.timeZone = TimeZone.getTimeZone("UTC")
        f.parse(iso)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

fun usageMillisToIso(millis: Long): String {
    val f = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    f.timeZone = TimeZone.getTimeZone("UTC")
    return f.format(Date(millis))
}
