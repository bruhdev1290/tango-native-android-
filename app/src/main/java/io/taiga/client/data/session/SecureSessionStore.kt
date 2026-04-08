package io.taiga.client.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val sessionState = MutableStateFlow(readSessionFromPreferences())

    fun observeSession(): StateFlow<UserSession?> = sessionState.asStateFlow()

    fun readSession(): UserSession? {
        return readSessionFromPreferences()
    }

    private fun readSessionFromPreferences(): UserSession? {
        val baseUrl = preferences.getString(KEY_BASE_URL, null) ?: return null
        val id = preferences.getLong(KEY_ID, -1L)
        val username = preferences.getString(KEY_USERNAME, null) ?: return null
        val authToken = preferences.getString(KEY_AUTH_TOKEN, null) ?: return null
        val refreshToken = preferences.getString(KEY_REFRESH_TOKEN, null) ?: return null

        return UserSession(
            baseUrl = baseUrl,
            id = id,
            username = username,
            displayName = preferences.getString(KEY_DISPLAY_NAME, null),
            email = preferences.getString(KEY_EMAIL, null),
            authToken = authToken,
            refreshToken = refreshToken,
        )
    }

    fun saveSession(session: UserSession) {
        preferences.edit()
            .putString(KEY_BASE_URL, session.baseUrl)
            .putLong(KEY_ID, session.id)
            .putString(KEY_USERNAME, session.username)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_AUTH_TOKEN, session.authToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .apply()

        sessionState.value = session
    }

    fun clear() {
        preferences.edit().clear().apply()
        sessionState.value = null
    }

    companion object {
        private const val FILE_NAME = "taiga_secure_session"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_ID = "id"
        private const val KEY_USERNAME = "username"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
