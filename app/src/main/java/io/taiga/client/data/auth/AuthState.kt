package io.taiga.client.data.auth

/**
 * Auth UI state machine.
 * Represents the current authentication state exposed to the UI.
 */
sealed class AuthState {
    /**
     * Idle state - no active authentication operation.
     * User is not authenticated.
     */
    data object Idle : AuthState()

    /**
     * Loading state - authentication operation in progress.
     * This could be login, refresh, or token restoration.
     */
    data object Loading : AuthState()

    /**
     * Authenticated state - user has valid token.
     * @param token The current authentication token with metadata
     * @param baseUrl The normalized base URL for the authenticated instance
     */
    data class Authenticated(
        val token: AuthToken,
        val baseUrl: String,
    ) : AuthState()

    /**
     * Failed state - authentication operation failed.
     * @param message Error message to display to the user
     * @param error The specific error type if available
     */
    data class Failed(
        val message: String,
        val error: TaigaAuthError? = null,
    ) : AuthState()
}
