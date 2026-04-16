package io.taiga.client.data.lock

import android.content.Context
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

interface AppLockRepository {
    val pinEnabledFlow: kotlinx.coroutines.flow.StateFlow<Boolean>
    val biometricEnabledFlow: kotlinx.coroutines.flow.StateFlow<Boolean>
    fun isPinEnabled(): Boolean
    fun isBiometricEnabled(): Boolean
    fun isBiometricAvailable(): Boolean
    fun isLockEnabled(): Boolean
    fun verifyPin(pin: String): Boolean
    suspend fun setupPin(pin: String)
    suspend fun removePin()
    suspend fun setBiometricEnabled(enabled: Boolean)
}

@Singleton
class AppLockRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppLockRepository {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val _pinEnabledFlow = MutableStateFlow(prefs.getString(KEY_PIN_HASH, null) != null)
    override val pinEnabledFlow: StateFlow<Boolean> = _pinEnabledFlow.asStateFlow()

    private val _biometricEnabledFlow = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    override val biometricEnabledFlow: StateFlow<Boolean> = _biometricEnabledFlow.asStateFlow()

    override fun isPinEnabled(): Boolean = _pinEnabledFlow.value

    override fun isBiometricEnabled(): Boolean = _biometricEnabledFlow.value

    override fun isBiometricAvailable(): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun isLockEnabled(): Boolean = isPinEnabled()

    override fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val candidate = hashPin(pin, salt)
        return MessageDigest.isEqual(candidate.toByteArray(), storedHash.toByteArray())
    }

    override suspend fun setupPin(pin: String) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .apply()
        _pinEnabledFlow.value = true
    }

    override suspend fun removePin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
        _pinEnabledFlow.value = false
        _biometricEnabledFlow.value = false
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _biometricEnabledFlow.value = enabled
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$pin$salt".toByteArray(Charsets.UTF_8)
        val hashBytes = digest.digest(input)
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    companion object {
        private const val FILE_NAME = "taiga_app_lock"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
}
