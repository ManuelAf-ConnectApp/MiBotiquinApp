package com.connectapp.data.repository

import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.AuthRemoteDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.mapper.toEntity
import com.connectapp.domain.model.AppError
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val userDao: UserDao
) : AuthRepository {
    override suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean
    ): Result<User> {
        if (authLocalDataSource.getToken() != null) {
            val user = userDao.getUserOnce()?.toDomain()
            if (user != null) {
                return Result.success(user)
            }
        }

        return authRemoteDataSource.login(email, password).fold(
            onSuccess = { userDto ->
                try {
                    val userEntity = userDto.toEntity()
                    if (userDao.getUserOnce()?.id?.toString() != userEntity.id.toString()) {
                        userDao.clearAndInsertUser(userEntity)
                    }

                    if (rememberMe) {
                        authLocalDataSource.saveToken("mock_token_${userDto.id}")
                        authLocalDataSource.saveRememberMe(true)
                    }
                    val persistedUser = userDao.getUserOnce()?.toDomain() ?: userDto.toDomain()
                    Result.success(persistedUser)
                } catch (e: Exception) {
                    Result.failure(AppError.UnknownError(e.message))
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<User> {
        return authRemoteDataSource.register(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password
        ).fold(
            onSuccess = { userDto ->
                try {
                    val userEntity = userDto.toEntity()
                    userDao.clearAndInsertUser(userEntity)
                    // Guardar token tras registro exitoso para evitar logout inmediato en iOS
                    authLocalDataSource.saveToken("mock_token_${userDto.id}")
                    val persistedUser = userDao.getUserOnce()?.toDomain() ?: userDto.toDomain()
                    Result.success(persistedUser)
                } catch (e: Exception) {
                    Result.failure(AppError.CustomError(e.message ?: "Unknown database error"))
                }
            },
            onFailure = { 
                val mappedError = if (it is AppError) it else AppError.CustomError(it.message ?: "Unknown network error")
                Result.failure(mappedError)
            }
        )
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return if (authLocalDataSource.getToken() != null) {
            val user = userDao.getUserOnce()?.toDomain()
            Result.success(user)
        } else {
            Result.success(null)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return authRemoteDataSource.forgotPassword(email)
    }

    override suspend fun logout(): Result<Unit> {
        authLocalDataSource.clearSession()
        userDao.clearUser()
        return Result.success(Unit)
    }

    override suspend fun deleteAccount(email: String): Result<Unit> {
        return authRemoteDataSource.deleteAccount(email).onSuccess {
            authLocalDataSource.clearSession()
            userDao.clearUser()
        }
    }

}
