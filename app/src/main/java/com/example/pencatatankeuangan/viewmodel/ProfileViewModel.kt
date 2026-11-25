package com.example.pencatatankeuangan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.pencatatankeuangan.data.SupabaseConfig
import com.example.pencatatankeuangan.data.SupabaseRest
import com.example.pencatatankeuangan.model.UserProfile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val message: String? = null
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState()
    )

    val uiState: StateFlow<ProfileUiState> =
        _uiState.asStateFlow()

    init {
        loadProfile()
    }

    // READ: nama dan email dari akun Supabase yang sedang login
    fun loadProfile() {
        viewModelScope.launch {
            try {
                val json = JSONObject(
                    SupabaseRest.request("GET", "/auth/v1/user")
                )

                val email = json.optString("email", "")

                val metadataName = json
                    .optJSONObject("user_metadata")
                    ?.optString("full_name", "")
                    .orEmpty()

                _uiState.value = _uiState.value.copy(
                    profile = _uiState.value.profile.copy(
                        email = email,
                        // Jika nama belum pernah diisi, pakai bagian depan email
                        fullName = metadataName.ifBlank {
                            email.substringBefore("@")
                        }
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Gagal memuat profil: ${e.message.orEmpty()}"
                )
            }
        }
    }

    // UPDATE PROFIL (disimpan juga ke akun Supabase)
    fun updateProfile(name: String) {
        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile.copy(
                fullName = name
            )
        )

        viewModelScope.launch {
            try {
                SupabaseRest.request(
                    method = "PUT",
                    path = "/auth/v1/user",
                    body = JSONObject().put(
                        "data",
                        JSONObject().put("full_name", name.trim())
                    )
                )

                _uiState.value = _uiState.value.copy(
                    message = "Perubahan berhasil disimpan"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = "Gagal menyimpan nama: ${e.message.orEmpty()}"
                )
            }
        }
    }

    // UBAH KATA SANDI (verifikasi sandi lama, lalu simpan sandi baru ke Supabase Auth)
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val email = _uiState.value.profile.email
                if (email.isBlank()) {
                    onResult(false, "Data akun belum dimuat. Coba lagi sebentar.")
                    return@launch
                }

                if (!verifyPassword(email, currentPassword)) {
                    onResult(false, "Kata sandi saat ini salah.")
                    return@launch
                }

                SupabaseRest.request(
                    method = "PUT",
                    path = "/auth/v1/user",
                    body = JSONObject().put("password", newPassword)
                )

                onResult(true, "Kata sandi berhasil diubah.")
            } catch (e: Exception) {
                onResult(
                    false,
                    e.message?.takeIf { it.isNotBlank() }
                        ?: "Gagal mengubah kata sandi."
                )
            }
        }
    }

    // Cek sandi lama tanpa mengganti session yang sedang aktif
    private suspend fun verifyPassword(
        email: String,
        password: String
    ): Boolean = withContext(Dispatchers.IO) {
        val connection = URL(
            "${SupabaseConfig.url}/auth/v1/token?grant_type=password"
        ).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("apikey", SupabaseConfig.anonKey)
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use {
                it.write(
                    JSONObject()
                        .put("email", email)
                        .put("password", password)
                        .toString()
                        .toByteArray(Charsets.UTF_8)
                )
            }
            connection.responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    }

    // UPDATE FOTO PROFIL
    fun updatePhoto(uri: String) {

        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile.copy(
                photoUri = uri
            ),
            message = "Foto profil berhasil diperbarui"
        )
    }

    // PENGATURAN NOTIFIKASI
    fun toggleNotification(enabled: Boolean) {

        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile.copy(
                notificationEnabled = enabled
            )
        )
    }

    // HAPUS PESAN
    fun clearMessage() {

        _uiState.value = _uiState.value.copy(
            message = null
        )
    }
}
