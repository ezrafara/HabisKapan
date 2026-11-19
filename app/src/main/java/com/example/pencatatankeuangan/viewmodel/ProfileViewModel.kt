package com.example.pencatatankeuangan.viewmodel

import androidx.lifecycle.ViewModel

import com.example.pencatatankeuangan.model.UserProfile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    // UPDATE PROFIL
    fun updateProfile(name: String) {

        _uiState.value = _uiState.value.copy(
            profile = _uiState.value.profile.copy(
                fullName = name
            ),
            message = "Perubahan berhasil disimpan"
        )
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
