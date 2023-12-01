package com.example.efarm.core.domain.usecase

import com.example.efarm.core.data.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthUseCase {
    fun getCurrentUser(): FirebaseUser?
    suspend fun registerAccount(
        email: String,
        pass: String,
        name: String,
        telepon: String,
    ): Flow<Resource<String>>

    suspend fun login(email: String, pass: String): Flow<Resource<String>>
}