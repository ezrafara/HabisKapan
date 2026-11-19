
package com.example.pencatatankeuangan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.sessionDataStore: DataStore<Preferences> by
preferencesDataStore(name = "habiskapan_session")

class SessionDataStore(
    private val context: Context
) {

    companion object {
        private val ACCESS_TOKEN =
            stringPreferencesKey("access_token")

        private val REFRESH_TOKEN =
            stringPreferencesKey("refresh_token")

        private val USER_ID =
            stringPreferencesKey("user_id")
    }

    data class Session(
        val accessToken: String,
        val refreshToken: String,
        val userId: String
    )

    // Menyimpan sesi login
    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String
    ) {
        context.sessionDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[USER_ID] = userId
        }
    }

    // Mengambil sesi yang tersimpan
    suspend fun getSession(): Session? {
        val preferences = context.sessionDataStore.data.first()

        val accessToken = preferences[ACCESS_TOKEN]
        val refreshToken = preferences[REFRESH_TOKEN]
        val userId = preferences[USER_ID]

        return if (
            !accessToken.isNullOrBlank() &&
            !refreshToken.isNullOrBlank() &&
            !userId.isNullOrBlank()
        ) {
            Session(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId
            )
        } else {
            null
        }
    }

    // Menghapus sesi saat logout
    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
