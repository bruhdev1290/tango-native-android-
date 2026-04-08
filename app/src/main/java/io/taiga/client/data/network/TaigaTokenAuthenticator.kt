package io.taiga.client.data.network

import io.taiga.client.data.auth.RefreshRequest
import io.taiga.client.data.session.SecureSessionStore
import io.taiga.client.data.session.UserSession
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaigaTokenAuthenticator @Inject constructor(
    private val sessionStore: SecureSessionStore,
    private val serviceFactory: TaigaServiceFactory,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val session = sessionStore.readSession() ?: return null
        val currentAuthHeader = response.request.header("Authorization")
        if (currentAuthHeader != null && currentAuthHeader != "Bearer ${session.authToken}") {
            return null
        }

        val refreshedSession = refreshSession(session) ?: run {
            sessionStore.clear()
            return null
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${refreshedSession.authToken}")
            .build()
    }

    private fun refreshSession(session: UserSession): UserSession? {
        val refreshResponse = runCatching {
            serviceFactory.createAuthApi(session.baseUrl)
                .refresh(RefreshRequest(refresh = session.refreshToken))
                .execute()
        }.getOrNull() ?: return null

        if (!refreshResponse.isSuccessful) return null

        val body = refreshResponse.body() ?: return null
        val updatedSession = session.copy(
            authToken = body.auth_token,
            refreshToken = body.refresh ?: session.refreshToken,
        )
        sessionStore.saveSession(updatedSession)
        return updatedSession
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
