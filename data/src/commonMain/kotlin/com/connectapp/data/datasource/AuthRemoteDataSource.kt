package com.connectapp.data.datasource

import com.connectapp.data.interceptor.NetworkInterceptor
import com.connectapp.data.model.UserDto

interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun register(name: String, email: String, password: String): Result<UserDto>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}

class AuthRemoteDataSourceImpl(
    private val networkInterceptor: NetworkInterceptor
) : AuthRemoteDataSource {
    override suspend fun login(email: String, password: String): Result<UserDto> {
        return networkInterceptor.execute {
            // Aquí iría la llamada real a la API, por ahora simulamos éxito
            val mockId = if (email == "admin@admin.com") "admin" else "1"
            UserDto(
                id = mockId,
                email = email,
                firstName = "Mock",
                lastName = "Mock User",
                phone = "123456789",
                password = password,
                createdAt = "2023-01-01",
                updatedAt = "2023-01-01"
            )
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<UserDto> {
        return networkInterceptor.execute {
            UserDto(
                id = "2",
                email = email,
                firstName = name.split(" ").firstOrNull() ?: "Mock",
                lastName = name.split(" ").getOrNull(1) ?: "User",
                phone = "987654321",
                password = password,
                createdAt = "2023-01-01",
                updatedAt = "2023-01-01"
            )
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return networkInterceptor.execute {
            Unit
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return networkInterceptor.execute {
            Unit
        }
    }
}
