
package com.example.pencatatankeuangan.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

private const val MAX_IMAGE_BYTES = 5L * 1024 * 1024

class ProductImageRepository(
    private val context: Context
) {

    suspend fun uploadImage(uri: Uri): String =
        withContext(Dispatchers.IO) {

            val userId = SupabaseConfig.userId
                ?.takeIf { it.isNotBlank() }
                ?: throw IOException("User belum login.")

            val token = SupabaseConfig.accessToken
                ?.takeIf { it.isNotBlank() }
                ?: throw IOException("Session login tidak ditemukan.")

            val mimeType = context.contentResolver
                .getType(uri)
                ?: "image/jpeg"

            if (mimeType !in listOf(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
                )
            ) {
                throw IOException("Format gambar harus JPG, PNG, atau WEBP.")
            }

            val sizeBytes = context.contentResolver
                .openAssetFileDescriptor(uri, "r")
                ?.use { it.length }
                ?: -1L

            if (sizeBytes > MAX_IMAGE_BYTES) {
                throw IOException("Ukuran gambar maksimal 5 MB.")
            }

            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val fileName = "${UUID.randomUUID()}.$extension"
            val objectPath = "$userId/$fileName"

            val uploadUrl =
                "${SupabaseConfig.url}/storage/v1/object/product-images/$objectPath"

            val connection = URL(uploadUrl)
                .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.doOutput = true

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
                    mimeType
                )

                connection.setRequestProperty(
                    "x-upsert",
                    "false"
                )

                val input = context.contentResolver
                    .openInputStream(uri)
                    ?: throw IOException("Gambar tidak dapat dibaca.")

                input.use { source ->
                    connection.outputStream.use { output ->
                        source.copyTo(output)
                    }
                }

                val code = connection.responseCode

                if (code !in 200..299) {
                    val error = connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        .orEmpty()

                    throw IOException(
                        error.ifBlank {
                            "Upload gambar gagal. Kode: $code"
                        }
                    )
                }

                "${SupabaseConfig.url}/storage/v1/object/public/product-images/$objectPath"

            } finally {
                connection.disconnect()
            }
        }
}
