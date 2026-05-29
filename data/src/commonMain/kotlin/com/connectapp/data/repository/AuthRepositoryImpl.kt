package com.connectapp.data.repository

import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.AuthRemoteDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        if (authLocalDataSource.getToken() != null) {
            // Simulate fetching user data from local storage if token exists
            return Result.success(
                User(
                    firstName = "Cached",
                    lastName = "User",
                    email = email,
                    phone = "123456789"
                )
            )
        }
        /**
        In this case, you might receive the token instead of the user, since some back-end systems
        return the user in a second request
        **/
        return authRemoteDataSource.login(email, password).map {
            it.toDomain()
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Boolean> {
        return authRemoteDataSource.register(
            name = name,
            email = email,
            password = password
        )
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return authRemoteDataSource.forgotPassword(email)
    }

}