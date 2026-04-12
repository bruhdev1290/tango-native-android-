package io.taiga.client.data.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthToken(
    val id: Long,
    val username: String,
    val auth_token: String,
    val refresh: String,
    val full_name_display: String? = null,
    val email: String? = null,
    val expires: Long? = null,
)

data class NormalLoginRequest(
    val type: String = "normal",
    val username: String,
    val password: String,
)

data class GitHubLoginRequest(
    val type: String = "github",
    val code: String,
)

data class RefreshRequest(
    val refresh: String,
)

sealed class TaigaAuthError(message: String) : Exception(message) {
    data class InvalidCredentials(override val message: String = "Invalid credentials") : TaigaAuthError(message)
    data class HttpError(val status: Int, override val message: String) : TaigaAuthError(message)
    data class GitHubAuthFailed(val detail: String, override val message: String = "GitHub auth failed: $detail") : TaigaAuthError(message)
    data class NetworkError(override val message: String) : TaigaAuthError(message)
    data class UnknownError(override val message: String) : TaigaAuthError(message)
}
