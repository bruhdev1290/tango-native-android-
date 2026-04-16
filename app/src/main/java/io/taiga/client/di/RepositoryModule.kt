package io.taiga.client.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.taiga.client.data.auth.AuthApi
import io.taiga.client.data.auth.AuthRepository
import io.taiga.client.data.auth.AuthRepositoryImpl
import io.taiga.client.data.items.ItemsRepository
import io.taiga.client.data.items.ItemsRepositoryImpl
import io.taiga.client.data.lock.AppLockRepository
import io.taiga.client.data.lock.AppLockRepositoryImpl
import io.taiga.client.data.session.SecureSessionStore
import io.taiga.client.data.workspace.TaigaWorkspaceRepository
import io.taiga.client.data.workspace.TaigaWorkspaceRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        sessionStore: SecureSessionStore,
        authApiProvider: @JvmSuppressWildcards (String) -> AuthApi,
    ): AuthRepository {
        return AuthRepositoryImpl(
            sessionStore = sessionStore,
            authApiProvider = authApiProvider,
        )
    }

    @Provides
    @Singleton
    fun provideWorkspaceRepository(
        impl: TaigaWorkspaceRepositoryImpl,
    ): TaigaWorkspaceRepository = impl

    @Provides
    @Singleton
    fun provideItemsRepository(
        impl: ItemsRepositoryImpl,
    ): ItemsRepository = impl

    @Provides
    @Singleton
    fun provideAppLockRepository(
        impl: AppLockRepositoryImpl,
    ): AppLockRepository = impl
}
