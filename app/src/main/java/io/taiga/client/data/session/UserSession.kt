package io.taiga.client.data.session

data class UserSession(
    val baseUrl: String,
    val id: Long,
    val username: String,
    val displayName: String?,
    val email: String?,
    val authToken: String,
    val refreshToken: String,
)
