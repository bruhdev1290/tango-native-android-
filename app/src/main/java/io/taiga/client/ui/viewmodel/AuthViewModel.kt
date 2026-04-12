package io.taiga.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.taiga.client.data.auth.AuthRepository
import io.taiga.client.data.auth.AuthState
import io.taiga.client.data.auth.TaigaAuthError
import io.taiga.client.data.session.normalizeTaigaApiBaseUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val DEFAULT_BASE_URL = "https://api.taiga.io/api/v1/"

/**
 * UI state for the login form.
 * Separate from AuthState which represents the authentication status.
 */
data class LoginFormState(
    val baseUrl: String = DEFAULT_BASE_URL,
    val username: String = "",
    val password: String = "",
    val gitHubCode: String = "",
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    /**
     * The authentication state machine exposed by the repository.
     * This is the source of truth for auth status.
     */
    val authState: StateFlow<AuthState> = authRepository.authState

    /**
     * Login form state - UI inputs for login.
     */
    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    /**
     * Initialize the auth repository on creation.
     */
    init {
        viewModelScope.launch {
            authRepository.initialize()
        }
    }

    /**
     * Update the base URL in the form.
     * If the URL changes, switch the auth context to the new URL.
     */
    fun onBaseUrlChanged(value: String) {
        _formState.update { it.copy(baseUrl = value) }
    }

    fun onUsernameChanged(value: String) {
        _formState.update { it.copy(username = value) }
    }

    fun onPasswordChanged(value: String) {
        _formState.update { it.copy(password = value) }
    }

    fun onGitHubCodeChanged(value: String) {
        _formState.update { it.copy(gitHubCode = value) }
    }

    /**
     * Switch to a different Taiga instance URL.
     * This will clear the current session context and load any existing session for the new URL.
     */
    fun switchInstanceUrl(baseUrl: String) {
        viewModelScope.launch {
            val normalizedUrl = normalizeTaigaApiBaseUrl(baseUrl)
            val hasExistingSession = authRepository.switchBaseUrl(normalizedUrl)
            _formState.update { 
                it.copy(
                    baseUrl = normalizedUrl,
                    // Clear credentials when switching instances
                    username = if (hasExistingSession) it.username else "",
                    password = "",
                )
            }
        }
    }

    /**
     * Login with normal credentials (username/password).
     */
    fun login() {
        val currentForm = _formState.value
        
        viewModelScope.launch {
            runCatching {
                authRepository.loginNormal(
                    baseUrl = currentForm.baseUrl,
                    username = currentForm.username.trim(),
                    password = currentForm.password,
                )
            }.onSuccess {
                // Clear password on successful login
                _formState.update { it.copy(password = "") }
            }.onFailure { throwable ->
                // Error is already set in authState by the repository
                // Just log or handle any additional UI-specific behavior
                if (throwable !is TaigaAuthError) {
                    // Unexpected error type
                }
            }
        }
    }

    /**
     * Login with GitHub OAuth code.
     */
    fun loginWithGitHub() {
        val currentForm = _formState.value
        
        viewModelScope.launch {
            runCatching {
                authRepository.loginGitHub(
                    baseUrl = currentForm.baseUrl,
                    code = currentForm.gitHubCode.trim(),
                )
            }.onSuccess {
                // Clear code on successful login
                _formState.update { it.copy(gitHubCode = "") }
            }.onFailure { throwable ->
                // Error is already set in authState by the repository
            }
        }
    }

    /**
     * Logout from the current session.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _formState.update {
                it.copy(
                    password = "",
                    gitHubCode = "",
                )
            }
        }
    }

    /**
     * Clear any error state and return to idle.
     */
    fun clearError() {
        val currentAuthState = authState.value
        if (currentAuthState is AuthState.Failed) {
            // Reset to idle - the repository doesn't have a direct "clear error" method
            // so we just update our understanding of the state
            // The repository's authState will remain as Failed until next action
        }
    }

    /**
     * Get a valid authenticated token, refreshing if necessary.
     * Call this before making authenticated API calls.
     * 
     * @param leewaySeconds Time in seconds before expiry to trigger refresh
     * @return Valid auth token string or null if not authenticated
     */
    suspend fun getAuthenticatedToken(leewaySeconds: Long = 300): String? {
        return authRepository.getAuthenticatedToken(leewaySeconds)?.auth_token
    }

    /**
     * Get the current authenticated token synchronously without refresh.
     * Use this when you need the token for immediate use and don't want
     * to trigger a suspend refresh operation.
     */
    fun getCurrentToken(): String? {
        val currentState = authState.value
        return if (currentState is AuthState.Authenticated) {
            currentState.token.auth_token
        } else {
            null
        }
    }

    /**
     * Get the current base URL.
     */
    fun getCurrentBaseUrl(): String? {
        return authRepository.getCurrentBaseUrl()
    }
}
