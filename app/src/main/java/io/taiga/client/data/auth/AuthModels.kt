package io.taiga.client.data.auth

data class LoginRequest(
    val type: String = "normal",
    val username: String,
    val password: String,
)

data class RefreshRequest(
    val refresh: String,
)

data class LoginResponse(
    val id: Long,
    val username: String,
    val auth_token: String,
    val refresh: String,
    val full_name_display: String? = null,
    val email: String? = null,
)

data class RefreshResponse(
    val auth_token: String,
    val refresh: String? = null,
)
