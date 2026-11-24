package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.Pembelian
import com.example.pencatatankeuangan.model.PembelianRequest
import org.json.JSONArray
import org.json.JSONObject

// Riwayat pembelian disimpan di tabel Supabase "purchases".
class PurchaseRepository {

    // READ: mengambil semua pembelian milik user
    suspend fun getAll(): List<Pembelian> {
        val userId = SupabaseRest.requireUserId()

        val text = SupabaseRest.request(
            method = "GET",
            path = "/rest/v1/purchases?select=*&user_id=eq.$userId" +
                    "&order=purchase_date.desc,id.desc"
        )

        val array = JSONArray(text)

        return List(array.length()) { index ->
            toPembelian(array.getJSONObject(index))
        }
    }

    // READ: mengambil detail berdasarkan ID
    suspend fun getById(id: Long): Pembelian? {
        val userId = SupabaseRest.requireUserId()

        val text = SupabaseRest.request(
            method = "GET",
            path = "/rest/v1/purchases?select=*&id=eq.$id&user_id=eq.$userId"
        )

        val array = JSONArray(text)

        return if (array.length() == 0) null
        else toPembelian(array.getJSONObject(0))
    }

    // CREATE: menambahkan pembelian
    suspend fun add(request: PembelianRequest) {
        val body = toJson(request)
            .put("user_id", SupabaseRest.requireUserId())

        SupabaseRest.request(
            method = "POST",
            path = "/rest/v1/purchases",
            body = body
        )
    }

    // UPDATE: mengubah pembelian
    suspend fun update(
        id: Long,
        request: PembelianRequest
    ) {
        val userId = SupabaseRest.requireUserId()

        SupabaseRest.request(
            method = "PATCH",
            path = "/rest/v1/purchases?id=eq.$id&user_id=eq.$userId",
            body = toJson(request)
        )
    }

    // DELETE: menghapus pembelian
    suspend fun delete(id: Long) {
        val userId = SupabaseRest.requireUserId()

        SupabaseRest.request(
            method = "DELETE",
            path = "/rest/v1/purchases?id=eq.$id&user_id=eq.$userId"
        )
    }

    private fun toJson(request: PembelianRequest) = JSONObject().apply {
        put("product_name", request.namaProduk)
        put("brand", request.merek ?: JSONObject.NULL)
        put("category", request.kategori ?: JSONObject.NULL)
        put("store", request.toko)
        put("purchase_date", request.tanggal)
        put("quantity", request.jumlah)
        put("price_per_item", request.hargaPerItem)
        put("image_url", request.imageUrl ?: JSONObject.NULL)
    }

    private fun JSONObject.stringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun toPembelian(item: JSONObject) = Pembelian(
        id = item.getLong("id"),
        namaProduk = item.getString("product_name"),
        merek = item.stringOrNull("brand"),
        kategori = item.stringOrNull("category"),
        toko = item.optString("store", ""),
        tanggal = item.optString("purchase_date", "").take(10),
        jumlah = item.optInt("quantity", 1),
        hargaPerItem = item.optLong("price_per_item", 0L),
        imageUrl = item.stringOrNull("image_url")
    )
}
