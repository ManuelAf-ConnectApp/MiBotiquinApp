package com.connectapp.data.datasource

import com.connectapp.data.interceptor.NetworkInterceptor
import com.connectapp.data.model.UserDto

interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun register(name: String, email: String, password: String): Result<Boolean>
    suspend fun forgotPassword(email: String): Result<Unit>
}

class AuthRemoteDataSourceImpl(
    private val networkInterceptor: NetworkInterceptor
) : AuthRemoteDataSource {
    override suspend fun login(email: String, password: String): Result<UserDto> {
        return networkInterceptor.execute {
            // Aquí iría la llamada real a la API, por ahora simulamos éxito
            UserDto(
                id = "1",
                email = email,
                firstName = "Mock",
                lastName = "Mock User",
                phone = "123456789",
                createdAt = "2023-01-01",
                updatedAt = "2023-01-01"
            )
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<Boolean> {
        return networkInterceptor.execute {
            true
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return networkInterceptor.execute {
            Unit
        }
    }
}
