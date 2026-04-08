package io.taiga.client.data.auth

import io.taiga.client.data.network.TaigaServiceFactory
import io.taiga.client.data.session.SecureSessionStore
import io.taiga.client.data.session.UserSession
import io.taiga.client.data.session.normalizeTaigaApiBaseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val serviceFactory: TaigaServiceFactory,
    private val sessionStore: SecureSessionStore,
) : AuthRepository {
    override suspend fun restoreSession(): UserSession? = sessionStore.readSession()

    override suspend fun login(baseUrl: String, username: String, password: String): UserSession {
        val normalizedBaseUrl = normalizeTaigaApiBaseUrl(baseUrl)
        val response = serviceFactory.createAuthApi(normalizedBaseUrl).login(
            LoginRequest(
                username = username,
                password = password,
            ),
        )

        val session = response.toSession(normalizedBaseUrl)
        sessionStore.saveSession(session)
        return session
    }

    override suspend fun refresh(session: UserSession): UserSession {
        val response = withContext(Dispatchers.IO) {
            serviceFactory.createAuthApi(session.baseUrl)
                .refresh(RefreshRequest(refresh = session.refreshToken))
                .execute()
        }

        if (!response.isSuccessful) {
            throw IllegalStateException("Unable to refresh Taiga session")
        }

        val body = response.body() ?: throw IllegalStateException("Empty refresh response")

        val refreshedSession = session.copy(
            authToken = body.auth_token,
            refreshToken = body.refresh ?: session.refreshToken,
        )
        sessionStore.saveSession(refreshedSession)
        return refreshedSession
    }

    override suspend fun logout() {
        sessionStore.clear()
    }

    private fun LoginResponse.toSession(baseUrl: String): UserSession {
        return UserSession(
            baseUrl = baseUrl,
            id = id,
            username = username,
            displayName = full_name_display,
            email = email,
            authToken = auth_token,
            refreshToken = refresh,
        )
    }
}
