package io.taiga.client.data.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth")
    suspend fun loginNormal(@Body request: NormalLoginRequest): Response<AuthToken>

    @POST("auth")
    suspend fun loginGitHub(@Body request: GitHubLoginRequest): Response<AuthToken>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthToken>
}
