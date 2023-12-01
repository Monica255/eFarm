package com.example.efarm

import com.example.efarm.core.data.source.repository.AuthRepository
import com.example.efarm.core.domain.usecase.AuthInteractor
import com.example.efarm.core.domain.usecase.AuthUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthUseCase(authRepository: AuthRepository): AuthUseCase=AuthInteractor(authRepository)

}