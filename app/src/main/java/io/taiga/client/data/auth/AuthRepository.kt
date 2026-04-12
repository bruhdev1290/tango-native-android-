package io.taiga.client.data.auth

import io.taiga.client.data.session.SecureSessionStore
import io.taiga.client.data.session.normalizeTaigaApiBaseUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

/**
 * Repository interface for authentication operations.
 * This is the single source of truth for authentication state.
 */
interface AuthRepository {
    /**
     * Observable authentication state for UI.
     */
    val authState: StateFlow<AuthState>

    /**
     * Get the current active base URL.
     */
    fun getCurrentBaseUrl(): String?

    /**
     * Initialize the repository by loading any existing session.
     * Should be called once at app startup.
     */
    suspend fun initialize()

    /**
     * Get an authenticated token, refreshing if necessary.
     * Call this before making authenticated API calls.
     * 
     * @param leewaySeconds Time in seconds before expiry to trigger refresh (default 300s = 5min)
     * @return Valid auth token or null if not authenticated
     */
    suspend fun getAuthenticatedToken(leewaySeconds: Long = 300): AuthToken?

    /**
     * Login with normal credentials.
     * 
     * @param baseUrl The Taiga instance URL (will be normalized)
     * @param username User's username or email
     * @param password User's password
     * @throws TaigaAuthError on authentication failure
     */
    suspend fun loginNormal(baseUrl: String, username: String, password: String)

    /**
     * Login with GitHub OAuth code.
     * 
     * @param baseUrl The Taiga instance URL (will be normalized)
     * @param code OAuth code from GitHub
     * @throws TaigaAuthError on authentication failure
     */
    suspend fun loginGitHub(baseUrl: String, code: String)

    /**
     * Switch to a different Taiga instance.
     * Clears current session state and loads/switches to the new base URL.
     * 
     * @param baseUrl The new Taiga instance URL (will be normalized)
     * @return True if there was an existing session for this URL
     */
    suspend fun switchBaseUrl(baseUrl: String): Boolean

    /**
     * Logout from current session.
     * Clears the session for the current base URL.
     */
    suspend fun logout()

    /**
     * Clear all sessions across all base URLs.
     */
    suspend fun logoutAll()

    /**
     * Manually trigger a token refresh.
     * @return True if refresh was successful
     */
    suspend fun refreshToken(): Boolean
}

class AuthRepositoryImpl(
    private val sessionStore: SecureSessionStore,
    private val authApiProvider: (baseUrl: String) -> AuthApi,
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Mutex to prevent concurrent refresh operations
    private val refreshMutex = Mutex()

    override fun getCurrentBaseUrl(): String? {
        return sessionStore.getActiveBaseUrl()
    }

    override suspend fun initialize() {
        val baseUrl = sessionStore.getActiveBaseUrl()
        if (baseUrl != null) {
            val token = sessionStore.loadTokenForBaseUrl(baseUrl)
            if (token != null) {
                _authState.value = AuthState.Authenticated(token, baseUrl)
            } else {
                _authState.value = AuthState.Idle
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    override suspend fun getAuthenticatedToken(leewaySeconds: Long): AuthToken? {
        val currentState = _authState.value
        
        // If we're already authenticated, check if we need to refresh
        if (currentState is AuthState.Authenticated) {
            val token = currentState.token
            val needsRefresh = token.expires?.let { expiry ->
                val refreshThreshold = (System.currentTimeMillis() / 1000) + leewaySeconds
                expiry <= refreshThreshold
            } ?: false

            return if (needsRefresh) {
                // Attempt refresh, return new token if successful
                if (refreshToken()) {
                    (_authState.value as? AuthState.Authenticated)?.token
                } else {
                    // Refresh failed, token might still be valid, return it
                    // Caller will handle 401 if token is actually expired
                    token
                }
            } else {
                token
            }
        }

        return null
    }

    override suspend fun loginNormal(baseUrl: String, username: String, password: String) {
        _authState.value = AuthState.Loading

        val normalizedBaseUrl = normalizeTaigaApiBaseUrl(baseUrl)
        
        // Switch base URL first (clears old session state)
        sessionStore.switchBaseUrl(normalizedBaseUrl)

        val request = NormalLoginRequest(username = username, password = password)
        
        val result = runCatching {
            authApiProvider(normalizedBaseUrl).loginNormal(request)
        }

        result.fold(
            onSuccess = { response ->
                handleAuthResponse(response, normalizedBaseUrl)
            },
            onFailure = { throwable ->
                val error = TaigaAuthError.UnknownError(throwable.message ?: "Network error")
                _authState.value = AuthState.Failed(error.message, error)
                throw error
            }
        )
    }

    override suspend fun loginGitHub(baseUrl: String, code: String) {
        _authState.value = AuthState.Loading

        val normalizedBaseUrl = normalizeTaigaApiBaseUrl(baseUrl)
        
        // Switch base URL first (clears old session state)
        sessionStore.switchBaseUrl(normalizedBaseUrl)

        val request = GitHubLoginRequest(code = code)
        
        val result = runCatching {
            authApiProvider(normalizedBaseUrl).loginGitHub(request)
        }

        result.fold(
            onSuccess = { response ->
                handleGitHubAuthResponse(response, normalizedBaseUrl)
            },
            onFailure = { throwable ->
                val error = TaigaAuthError.UnknownError(throwable.message ?: "Network error")
                _authState.value = AuthState.Failed(error.message, error)
                throw error
            }
        )
    }

    override suspend fun switchBaseUrl(baseUrl: String): Boolean {
        val normalizedBaseUrl = normalizeTaigaApiBaseUrl(baseUrl)
        val existingToken = sessionStore.switchBaseUrl(normalizedBaseUrl)
        
        if (existingToken != null) {
            _authState.value = AuthState.Authenticated(existingToken, normalizedBaseUrl)
            return true
        } else {
            _authState.value = AuthState.Idle
            return false
        }
    }

    override suspend fun logout() {
        val currentBaseUrl = sessionStore.getActiveBaseUrl()
        if (currentBaseUrl != null) {
            sessionStore.clearSession(currentBaseUrl)
        }
        _authState.value = AuthState.Idle
    }

    override suspend fun logoutAll() {
        sessionStore.clearAll()
        _authState.value = AuthState.Idle
    }

    override suspend fun refreshToken(): Boolean = refreshMutex.withLock {
        val currentState = _authState.value
        if (currentState !is AuthState.Authenticated) {
            return false
        }

        val currentToken = currentState.token
        val baseUrl = currentState.baseUrl
        val refreshToken = currentToken.refresh

        val result = runCatching {
            authApiProvider(baseUrl).refresh(RefreshRequest(refresh = refreshToken))
        }

        return result.fold(
            onSuccess = { response ->
                if (response.isSuccessful) {
                    val newToken = response.body()
                    if (newToken != null) {
                        // Preserve fields that might not be in refresh response
                        val mergedToken = newToken.copy(
                            id = newToken.id.takeIf { it != 0L } ?: currentToken.id,
                            username = newToken.username.takeIf { !it.isNullOrBlank() } ?: currentToken.username,
                            full_name_display = newToken.full_name_display ?: currentToken.full_name_display,
                            email = newToken.email ?: currentToken.email,
                        )
                        sessionStore.saveToken(baseUrl, mergedToken)
                        _authState.value = AuthState.Authenticated(mergedToken, baseUrl)
                        true
                    } else {
                        false
                    }
                } else {
                    // Refresh failed, likely need to re-authenticate
                    _authState.value = AuthState.Failed("Session expired. Please login again.")
                    false
                }
            },
            onFailure = {
                false
            }
        )
    }

    private fun handleAuthResponse(response: Response<AuthToken>, baseUrl: String) {
        when {
            response.isSuccessful -> {
                val token = response.body()
                if (token != null) {
                    sessionStore.saveToken(baseUrl, token)
                    _authState.value = AuthState.Authenticated(token, baseUrl)
                } else {
                    val error = TaigaAuthError.UnknownError("Empty response body")
                    _authState.value = AuthState.Failed(error.message, error)
                    throw error
                }
            }
            response.code() == 400 -> {
                val error = TaigaAuthError.InvalidCredentials()
                _authState.value = AuthState.Failed(error.message, error)
                throw error
            }
            else -> {
                val error = TaigaAuthError.HttpError(
                    status = response.code(),
                    message = "HTTP ${response.code()}: ${response.message()}"
                )
                _authState.value = AuthState.Failed(error.message, error)
                throw error
            }
        }
    }

    private fun handleGitHubAuthResponse(response: Response<AuthToken>, baseUrl: String) {
        when {
            response.isSuccessful -> {
                val token = response.body()
                if (token != null) {
                    sessionStore.saveToken(baseUrl, token)
                    _authState.value = AuthState.Authenticated(token, baseUrl)
                } else {
                    val error = TaigaAuthError.UnknownError("Empty response body")
                    _authState.value = AuthState.Failed(error.message, error)
                    throw error
                }
            }
            response.code() == 400 -> {
                // Try to parse error detail from response
                val errorBody = response.errorBody()?.string()
                val detail = parseGitHubErrorDetail(errorBody)
                val error = TaigaAuthError.GitHubAuthFailed(detail = detail)
                _authState.value = AuthState.Failed(error.message, error)
                throw error
            }
            else -> {
                val error = TaigaAuthError.HttpError(
                    status = response.code(),
                    message = "HTTP ${response.code()}: ${response.message()}"
                )
                _authState.value = AuthState.Failed(error.message, error)
                throw error
            }
        }
    }

    private fun parseGitHubErrorDetail(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Unknown error"
        // Simple parsing - could be enhanced with JSON parsing if needed
        return errorBody
    }
}
