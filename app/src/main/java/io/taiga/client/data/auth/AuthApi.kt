package io.taiga.client.data.auth

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface AuthApi {
    @POST("auth")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")
    fun refresh(@Body request: RefreshRequest): Call<RefreshResponse>
}
