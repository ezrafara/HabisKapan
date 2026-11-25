
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
                message = friendlyError(e, "Registrasi gagal.")
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
                message = friendlyError(e, "Login gagal.")
            )
        }
    }

    // =========================
    // LUPA KATA SANDI (mode demo: tanpa verifikasi email)
    // Memanggil fungsi database reset_password_demo
    // =========================
    suspend fun resetPassword(
        email: String,
        newPassword: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = request(
                method = "POST",
                path = "/rest/v1/rpc/reset_password_demo",
                body = JSONObject()
                    .put("p_email", email.trim())
                    .put("p_new_password", newPassword)
            )

            if (response.optString("result") != "true") {
                throw IOException("Email tidak terdaftar.")
            }

            AuthResult(
                success = true,
                message = "Kata sandi berhasil diubah. Silakan masuk dengan kata sandi baru."
            )
        } catch (e: Exception) {
            AuthResult(
                success = false,
                message = friendlyError(e, "Gagal mengubah kata sandi.")
            )
        }
    }

    // =========================
    // PESAN ERROR BAHASA INDONESIA
    // =========================
    private fun friendlyError(e: Exception, fallback: String): String {
        if (
            e is java.net.UnknownHostException ||
            e is java.net.SocketTimeoutException ||
            e is java.net.ConnectException
        ) {
            return "Tidak dapat terhubung ke server. Periksa koneksi internet kamu."
        }

        val raw = e.message.orEmpty()
        val msg = raw.lowercase()

        return when {
            "invalid login credentials" in msg ->
                "Email atau kata sandi salah. Periksa kembali lalu coba lagi."
            "email not confirmed" in msg ->
                "Email belum dikonfirmasi. Buka email verifikasi dari HabisKapan terlebih dahulu."
            "already registered" in msg || "already been registered" in msg ->
                "Email ini sudah terdaftar. Silakan masuk."
            "rate limit" in msg || "too many" in msg || "seconds" in msg && "security purposes" in msg ->
                "Terlalu banyak percobaan. Tunggu beberapa saat lalu coba lagi."
            "token has expired" in msg || "otp_expired" in msg || "invalid" in msg && "token" in msg ->
                "Kode verifikasi salah atau sudah kedaluwarsa."
            "should be different" in msg || "same password" in msg ->
                "Kata sandi baru harus berbeda dari yang lama."
            "password should be at least" in msg || "weak" in msg ->
                "Kata sandi terlalu lemah. Gunakan minimal 8 karakter dengan huruf dan angka."
            "email address" in msg && "invalid" in msg ->
                "Alamat email tidak valid atau tidak dapat digunakan."
            raw.isBlank() -> fallback
            else -> raw
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
                try {
                    JSONObject(responseText)
                } catch (_: Exception) {
                    // Respons bukan objek JSON (mis. true/false dari RPC)
                    JSONObject().put("result", responseText.trim())
                }
            }

        } finally {
            connection.disconnect()
        }
    }
}
