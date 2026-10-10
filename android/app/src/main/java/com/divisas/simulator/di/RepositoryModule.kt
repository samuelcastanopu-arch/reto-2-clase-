package com.divisas.simulator.di

import com.divisas.simulator.data.repository.AdminRepositoryImpl
import com.divisas.simulator.data.repository.AuthRepositoryImpl
import com.divisas.simulator.data.repository.CurrencyRepositoryImpl
import com.divisas.simulator.data.repository.MediaRepositoryImpl
import com.divisas.simulator.data.repository.OperationRepositoryImpl
import com.divisas.simulator.domain.repository.AdminRepository
import com.divisas.simulator.domain.repository.AuthRepository
import com.divisas.simulator.domain.repository.CurrencyRepository
import com.divisas.simulator.domain.repository.MediaRepository
import com.divisas.simulator.domain.repository.OperationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(impl: CurrencyRepositoryImpl): CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindOperationRepository(impl: OperationRepositoryImpl): OperationRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository
}
