package io.taiga.client.data.network

import com.squareup.moshi.Moshi
import io.taiga.client.data.api.TaigaApi
import io.taiga.client.data.auth.AuthRepository
import io.taiga.client.data.auth.AuthState
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaigaServiceFactory @Inject constructor(
    private val moshi: Moshi,
    private val loggingInterceptor: HttpLoggingInterceptor,
    private val authRepository: AuthRepository,
) {
    /**
     * Creates an authenticated TaigaApi instance for the current session.
     * Automatically adds Bearer token to requests.
     * 
     * @throws IllegalStateException if not authenticated
     */
    suspend fun createAuthenticatedApi(): TaigaApi {
        val currentState = authRepository.authState.value
        
        if (currentState !is AuthState.Authenticated) {
            throw IllegalStateException("Not authenticated. Call login first.")
        }

        // Get authenticated token (with refresh if needed)
        val token = authRepository.getAuthenticatedToken(leewaySeconds = 300)
            ?: throw IllegalStateException("Failed to get authenticated token")

        val baseUrl = currentState.baseUrl

        // Capture the token value for the interceptor (interceptors can't be suspend)
        val authToken = token.auth_token

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $authToken")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TaigaApi::class.java)
    }

    /**
     * Creates a TaigaApi with a specific token for cases where you need
     * to make an authenticated request without going through the normal flow.
     */
    fun createApiWithToken(baseUrl: String, token: String): TaigaApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TaigaApi::class.java)
    }
}
