package com.connectapp.domain.repository

import com.connectapp.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<Boolean>
    suspend fun forgotPassword(email: String): Result<Unit>
}