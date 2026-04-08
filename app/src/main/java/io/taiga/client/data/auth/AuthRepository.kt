package io.taiga.client.data.auth

import io.taiga.client.data.session.UserSession

interface AuthRepository {
    suspend fun restoreSession(): UserSession?
    suspend fun login(baseUrl: String, username: String, password: String): UserSession
    suspend fun refresh(session: UserSession): UserSession
    suspend fun logout()
}
