package io.taiga.client.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import io.taiga.client.data.auth.AuthToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSessionStore @Inject constructor(
    @ApplicationContext context: Context,
    private val moshi: Moshi,
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

    private val authTokenAdapter = moshi.adapter(AuthToken::class.java)

    private val _currentBaseUrl = MutableStateFlow(readActiveBaseUrl())
    val currentBaseUrl: StateFlow<String?> = _currentBaseUrl.asStateFlow()

    private val _currentToken = MutableStateFlow(loadTokenForBaseUrl(readActiveBaseUrl()))
    val currentToken: StateFlow<AuthToken?> = _currentToken.asStateFlow()

    /**
     * Get the active base URL that was last set.
     */
    fun getActiveBaseUrl(): String? {
        return readActiveBaseUrl()
    }

    /**
     * Read the currently active base URL from preferences.
     */
    private fun readActiveBaseUrl(): String? {
        return preferences.getString(KEY_ACTIVE_BASE_URL, null)
    }

    /**
     * Load token for a specific base URL.
     * Returns null if no token exists for that URL.
     */
    fun loadTokenForBaseUrl(baseUrl: String?): AuthToken? {
        if (baseUrl.isNullOrBlank()) return null
        val normalizedUrl = normalizeTaigaApiBaseUrl(baseUrl)
        val tokenJson = preferences.getString(tokenKeyForBaseUrl(normalizedUrl), null) ?: return null
        return runCatching { authTokenAdapter.fromJson(tokenJson) }.getOrNull()
    }

    /**
     * Save token for a specific base URL.
     * Also sets this as the active base URL.
     */
    fun saveToken(baseUrl: String, token: AuthToken) {
        val normalizedUrl = normalizeTaigaApiBaseUrl(baseUrl)
        val tokenJson = authTokenAdapter.toJson(token)
        
        preferences.edit()
            .putString(KEY_ACTIVE_BASE_URL, normalizedUrl)
            .putString(tokenKeyForBaseUrl(normalizedUrl), tokenJson)
            .apply()
        
        _currentBaseUrl.value = normalizedUrl
        _currentToken.value = token
    }

    /**
     * Clear session for a specific base URL.
     */
    fun clearSession(baseUrl: String) {
        val normalizedUrl = normalizeTaigaApiBaseUrl(baseUrl)
        preferences.edit()
            .remove(tokenKeyForBaseUrl(normalizedUrl))
            .apply()
        
        if (_currentBaseUrl.value == normalizedUrl) {
            _currentToken.value = null
        }
    }

    /**
     * Clear all sessions and active base URL.
     */
    fun clearAll() {
        preferences.edit().clear().apply()
        _currentBaseUrl.value = null
        _currentToken.value = null
    }

    /**
     * Switch to a different base URL.
     * Returns the token for the new base URL if it exists.
     */
    fun switchBaseUrl(baseUrl: String): AuthToken? {
        val normalizedUrl = normalizeTaigaApiBaseUrl(baseUrl)
        val token = loadTokenForBaseUrl(normalizedUrl)
        
        preferences.edit()
            .putString(KEY_ACTIVE_BASE_URL, normalizedUrl)
            .apply()
        
        _currentBaseUrl.value = normalizedUrl
        _currentToken.value = token
        
        return token
    }

    /**
     * Generate the storage key for a token given a normalized base URL.
     */
    private fun tokenKeyForBaseUrl(normalizedBaseUrl: String): String {
        return "${KEY_TOKEN_PREFIX}${normalizedBaseUrl}"
    }

    companion object {
        private const val FILE_NAME = "taiga_secure_session"
        private const val KEY_ACTIVE_BASE_URL = "active_base_url"
        private const val KEY_TOKEN_PREFIX = "token_for_"
    }
}
