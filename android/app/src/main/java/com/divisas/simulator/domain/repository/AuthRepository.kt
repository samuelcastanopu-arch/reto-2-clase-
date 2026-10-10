package com.divisas.simulator.domain.repository

import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, pass: String): Resource<User>
    suspend fun register(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Resource<User>
    suspend fun logout(): Resource<Unit>
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun getCurrentUser(): User?
    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String,
        address: String?,
        documentType: String?,
        documentNumber: String?
    ): Resource<Unit>
}
