package com.connectapp.data.repository

import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.AuthRemoteDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.mapper.toEntity
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userDao: UserDao
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        if (authLocalDataSource.getToken() != null) {
            // Simulate fetching user data from local storage if token exists
            return Result.success(
                User(
                    id = "1",
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
    ): Result<User> {
        return authRemoteDataSource.register(
            name = name,
            email = email,
            password = password
        ).map { userDto ->
            val userEntity = userDto.toEntity()
            userDao.insertUser(userEntity)
            userDto.toDomain()
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return if (authLocalDataSource.getToken() != null) {
            Result.success(
                User(
                    id = "1",
                    firstName = "Cached",
                    lastName = "User",
                    email = "cached@user.com",
                    phone = "123456789"
                )
            )
        } else {
            Result.success(null)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return authRemoteDataSource.forgotPassword(email)
    }

    override suspend fun logout(): Result<Unit> {
        authLocalDataSource.clearSession()
        return Result.success(Unit)
    }

}
