
package com.example.pencatatankeuangan.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class SupabaseAuthRepository(
    private val sessionDataStore: SessionDataStore
) {

    data class AuthResult(
        val success: Boolean,
        val message: String,
        val needsEmailVerification: Boolean = false
    )

    // =========================
    // REGISTRASI
    // =========================
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {

        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put(
                "data",
                JSONObject().put("full_name", name.trim())
            )

        try {
            val response = request(
                method = "POST",
                path = "/auth/v1/signup",
                body = body
            )

            val user = response.optJSONObject("user") ?: response
            val accessToken = response.optString("access_token")
            val refreshToken = response.optString("refresh_token")
            val userId = user.optString("id")

            if (
                accessToken.isNotBlank() &&
                refreshToken.isNotBlank() &&
                userId.isNotBlank()
            ) {
                SupabaseConfig.accessToken = accessToken
                SupabaseConfig.userId = userId

                sessionDataStore.saveSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = userId
                )
            }

            AuthResult(
                success = true,
                message = if (accessToken.isBlank()) {
                    "Registrasi berhasil. Silakan periksa email untuk verifikasi akun."
                } else {
                    "Registrasi berhasil."
                },
                needsEmailVerification = accessToken.isBlank()
            )

        } catch (e: Exception) {
            AuthResult(
                success = false,
                message = e.message ?: "Registrasi gagal."
            )
        }
    }

    // =========================
    // LOGIN
    // =========================
    suspend fun login(
        email: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {

        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)

        try {
            val response = request(
                method = "POST",
                path = "/auth/v1/token?grant_type=password",
                body = body
            )

            val accessToken = response.optString("access_token")
            val refreshToken = response.optString("refresh_token")
            val user = response.optJSONObject("user")
            val userId = user?.optString("id").orEmpty()

            if (
                accessToken.isBlank() ||
                refreshToken.isBlank() ||
                userId.isBlank()
            ) {
                throw IOException("Data sesi login tidak lengkap.")
            }

            saveSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId
            )

            AuthResult(
                success = true,
                message = "Login berhasil."
            )

        } catch (e: Exception) {
            AuthResult(
                success = false,
                message = e.message ?: "Login gagal."
            )
        }
    }

    // =========================
    // SIMPAN SESI
    // =========================
    private suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String
    ) {
        SupabaseConfig.accessToken = accessToken
        SupabaseConfig.userId = userId

        sessionDataStore.saveSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId
        )
    }

    // =========================
    // PULIHKAN SESI
    // =========================
    suspend fun restoreSession(): Boolean =
        withContext(Dispatchers.IO) {

            try {
                val session = sessionDataStore.getSession()
                    ?: return@withContext false

                // Perbarui sesi menggunakan refresh token.
                val body = JSONObject()
                    .put("refresh_token", session.refreshToken)

                val response = request(
                    method = "POST",
                    path = "/auth/v1/token?grant_type=refresh_token",
                    body = body
                )

                val newAccessToken =
                    response.optString("access_token")

                val newRefreshToken =
                    response.optString("refresh_token")

                val user = response.optJSONObject("user")
                val newUserId = user?.optString("id")
                    ?.takeIf { it.isNotBlank() }
                    ?: session.userId

                if (
                    newAccessToken.isBlank() ||
                    newRefreshToken.isBlank()
                ) {
                    throw IOException("Sesi tidak dapat diperbarui.")
                }

                saveSession(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken,
                    userId = newUserId
                )

                true

            } catch (e: Exception) {
                SupabaseConfig.accessToken = null
                SupabaseConfig.userId = null

                // Hapus sesi yang sudah tidak valid.
                sessionDataStore.clearSession()

                false
            }
        }

    // =========================
    // LOGOUT
    // =========================
    suspend fun logout(): AuthResult =
        withContext(Dispatchers.IO) {

            val token = SupabaseConfig.accessToken

            return@withContext try {

                if (!token.isNullOrBlank()) {
                    request(
                        method = "POST",
                        path = "/auth/v1/logout",
                        bearerToken = token
                    )
                }

                AuthResult(
                    success = true,
                    message = "Logout berhasil."
                )

            } catch (e: Exception) {
                AuthResult(
                    success = false,
                    message = e.message ?: "Logout gagal."
                )

            } finally {
                // Tetap hapus sesi lokal saat logout.
                SupabaseConfig.accessToken = null
                SupabaseConfig.userId = null
                sessionDataStore.clearSession()
            }
        }

    // =========================
    // REQUEST KE SUPABASE
    // =========================
    private fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        bearerToken: String? = null
    ): JSONObject {

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
                "Bearer ${bearerToken ?: SupabaseConfig.anonKey}"
            )

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
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
                val error = try {
                    val json = JSONObject(responseText)

                    json.optString("msg").ifBlank {
                        json.optString("message").ifBlank {
                            json.optString("error_description")
                        }
                    }
                } catch (_: Exception) {
                    responseText
                }

                throw IOException(
                    error.ifBlank {
                        "Supabase error: $code"
                    }
                )
            }

            return if (responseText.isBlank()) {
                JSONObject()
            } else {
                JSONObject(responseText)
            }

        } finally {
            connection.disconnect()
        }
    }
}
