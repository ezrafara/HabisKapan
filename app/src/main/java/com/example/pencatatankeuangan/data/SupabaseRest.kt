package com.example.pencatatankeuangan.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// Helper HTTP bersama untuk repository yang memanggil Supabase PostgREST.
object SupabaseRest {

    fun requireUserId(): String =
        SupabaseConfig.userId
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException("User belum login.")

    // path diawali "/rest/v1/..."
    suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null
    ): String = withContext(Dispatchers.IO) {

        val token = SupabaseConfig.accessToken
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException("Session login tidak ditemukan.")

        val connection = URL("${SupabaseConfig.url}$path")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            connection.setRequestProperty("apikey", SupabaseConfig.anonKey)
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Prefer", "return=representation")

            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
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
                val message = try {
                    val json = JSONObject(responseText)
                    json.optString("message")
                        .ifBlank { json.optString("details") }
                        .ifBlank { json.optString("hint") }
                        .ifBlank { json.optString("error") }
                } catch (e: Exception) {
                    responseText
                }

                throw IOException(
                    message.ifBlank { "Supabase error: $code" }
                )
            }

            responseText
        } finally {
            connection.disconnect()
        }
    }
}
