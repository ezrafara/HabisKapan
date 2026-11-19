package com.example.pencatatankeuangan.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Pembelian(
    val id: Long = 0L,
    val namaProduk: String = "",
    val merek: String? = null,
    val kategori: String? = null,
    val toko: String = "",
    val tanggal: String = "",
    val jumlah: Int = 1,
    val hargaPerItem: Long = 0L,
    val imageUrl: String? = null
) {
    val totalHarga: Long
        get() = jumlah * hargaPerItem
}

data class PembelianRequest(
    val namaProduk: String,
    val merek: String?,
    val kategori: String?,
    val toko: String,
    val tanggal: String,
    val jumlah: Int,
    val hargaPerItem: Long,
    val imageUrl: String?
)

private val localeIndonesia = Locale.forLanguageTag("id-ID")

fun formatRupiah(value: Long): String {
    return "Rp " + NumberFormat
        .getNumberInstance(localeIndonesia)
        .format(value)
}

fun formatTanggal(tanggal: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat("d MMMM yyyy", localeIndonesia)
        output.format(input.parse(tanggal)!!)
    } catch (e: Exception) {
        tanggal
    }
}

fun bulanTahun(tanggal: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat("MMMM yyyy", localeIndonesia)
        output.format(input.parse(tanggal)!!)
    } catch (e: Exception) {
        tanggal
    }
}

fun todayIso(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US)
        .format(Date())
}
