package io.taiga.client.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.taiga.client.data.auth.AuthRepository
import io.taiga.client.data.auth.AuthRepositoryImpl
import io.taiga.client.data.workspace.TaigaWorkspaceRepository
import io.taiga.client.data.workspace.TaigaWorkspaceRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindWorkspaceRepository(impl: TaigaWorkspaceRepositoryImpl): TaigaWorkspaceRepository
}
