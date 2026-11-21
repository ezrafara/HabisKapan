
package com.example.pencatatankeuangan.data

import com.example.pencatatankeuangan.model.ProductItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ProductRepository {

    // READ: Mengambil produk milik user yang login
    suspend fun getProducts(): List<ProductItem> =
        withContext(Dispatchers.IO) {

            val userId = requireUserId()

            val response = request(
                method = "GET",
                path = "/rest/v1/products?select=*&user_id=eq.$userId&order=id.asc"
            )

            val jsonArray = JSONArray(response)

            List(jsonArray.length()) { index ->
                val item = jsonArray.getJSONObject(index)

                ProductItem(
                    id = item.getLong("id"),
                    name = item.getString("name"),
                    brand = item.getString("brand"),
                    category = item.getString("category"),
                    unit = item.getString("unit"),
                    stock = item.getInt("stock"),
                    note = item.optString("note", ""),
                    imageUrl = item.optString("image_url")
                        .takeIf { it.isNotBlank() && it != "null" }
                )
            }
        }

    // INSERT: Menambahkan produk
    suspend fun addProduct(product: ProductItem) =
        withContext(Dispatchers.IO) {

            val userId = requireUserId()

            val body = JSONObject().apply {
                put("user_id", userId)
                put("name", product.name)
                put("brand", product.brand)
                put("category", product.category)
                put("unit", product.unit)
                put("stock", product.stock)
                put("note", product.note)
                put("image_url", product.imageUrl)
            }

            request(
                method = "POST",
                path = "/rest/v1/products",
                body = body
            )
        }

    // UPDATE: Mengubah produk
    suspend fun updateProduct(product: ProductItem) =
        withContext(Dispatchers.IO) {

            val userId = requireUserId()

            val body = JSONObject().apply {
                put("name", product.name)
                put("brand", product.brand)
                put("category", product.category)
                put("unit", product.unit)
                put("stock", product.stock)
                put("note", product.note)
                put("image_url", product.imageUrl)
            }

            request(
                method = "PATCH",
                path = "/rest/v1/products?id=eq.${product.id}&user_id=eq.$userId",
                body = body
            )
        }

    // DELETE: Menghapus produk
    suspend fun deleteProduct(id: Long) =
        withContext(Dispatchers.IO) {

            val userId = requireUserId()

            request(
                method = "DELETE",
                path = "/rest/v1/products?id=eq.$id&user_id=eq.$userId"
            )
        }

    // Memastikan user sudah login
    private fun requireUserId(): String {
        return SupabaseConfig.userId
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException("User belum login.")
    }

    // REQUEST KE SUPABASE
    private fun request(
        method: String,
        path: String,
        body: JSONObject? = null
    ): String {

        val token = SupabaseConfig.accessToken
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException("Session login tidak ditemukan.")

        val connection = URL(
            "${SupabaseConfig.url}$path"
        ).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            connection.setRequestProperty(
                "apikey",
                SupabaseConfig.anonKey
            )

            connection.setRequestProperty(
                "Authorization",
                "Bearer $token"
            )

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "Prefer",
                "return=representation"
            )

            if (body != null) {
                connection.doOutput = true

                connection.outputStream.use { output ->
                    output.write(
                        body.toString().toByteArray(Charsets.UTF_8)
                    )
                }
            }

            val code = connection.responseCode

            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = stream
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (code !in 200..299) {
                val errorMessage = try {
                    val json = JSONObject(responseText)

                    json.optString("message")
                        .ifBlank {
                            json.optString("details")
                        }
                        .ifBlank {
                            json.optString("hint")
                        }
                        .ifBlank {
                            json.optString("error")
                        }
                } catch (e: Exception) {
                    responseText
                }

                throw IOException(
                    errorMessage.ifBlank {
                        "Supabase error: $code"
                    }
                )
            }

            return responseText

        } finally {
            connection.disconnect()
        }
    }
}
