package com.connectapp.data.datasource

import com.connectapp.data.interceptor.NetworkInterceptor
import com.connectapp.data.model.UserDto
import com.connectapp.domain.model.AppError
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Updated AuthRemoteDataSource
interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<UserDto>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun deleteAccount(email: String): Result<Unit>
}

class AuthRemoteDataSourceImpl(
    private val networkInterceptor: NetworkInterceptor
) : AuthRemoteDataSource {
    private val registeredEmails = mutableSetOf("admin@admin.com", "user@mail.com", "test@test.com")

    override suspend fun login(email: String, password: String): Result<UserDto> {
        if (!registeredEmails.contains(email)) {
            return Result.failure(AppError.UserNotFound())
        }
        return networkInterceptor.execute {
            val mockId = if (email == "admin@admin.com") "admin" else "1"
            UserDto(
                id = mockId,
                email = email,
                firstName = "Mock",
                lastName = "Mock",
                password = password,
                createdAt = "2023-01-01",
                updatedAt = "2023-01-01"
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<UserDto> {
        registeredEmails.add(email)
        return networkInterceptor.execute {
            val now = Clock.System.now().toString()
            val generatedId = Uuid.random().toString()
            UserDto(
                id = generatedId,
                email = email,
                firstName = firstName,
                lastName = lastName,
                password = password,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return networkInterceptor.execute {
            Result.success(Unit).getOrThrow()
        }
    }

    override suspend fun deleteAccount(email: String): Result<Unit> {
        return networkInterceptor.execute {
            registeredEmails.remove(email)
            Result.success(Unit).getOrThrow()
        }
    }
}
