package com.connectapp.data.repository

import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.AuthRemoteDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.AppError
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

// Los usuarios locales se identifican por email y conservan su id autogenerado entre
// sesiones: `medications.codUser` apunta a ese id. Nunca se borra la tabla users al
// iniciar o cerrar sesión, porque el ON DELETE CASCADE eliminaría sus medicamentos.
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
        val normalizedEmail = email.normalizeEmail()

        return authRemoteDataSource.login(normalizedEmail, password).fold(
            onSuccess = { userDto ->
                try {
                    val localUser = userDao.getUserByEmail(normalizedEmail)
                        ?: return Result.failure(AppError.InvalidCredentials())
                    authLocalDataSource.clearSession()
                    authLocalDataSource.saveCurrentUserId(localUser.id)
                    authLocalDataSource.saveRememberMe(rememberMe)

                    if (rememberMe) {
                        authLocalDataSource.saveToken("mock_token_${userDto.id}")
                    }
                    Result.success(localUser.toDomain())
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
        val normalizedEmail = email.normalizeEmail()

        return authRemoteDataSource.register(
            firstName = firstName,
            lastName = lastName,
            email = normalizedEmail,
            password = password
        ).fold(
            onSuccess = { userDto ->
                try {
                    val localUser = userDao.getUserByEmail(normalizedEmail)
                        ?: return Result.failure(AppError.UnknownError("Registered local account missing"))
                    authLocalDataSource.clearSession()
                    authLocalDataSource.saveCurrentUserId(localUser.id)
                    // Guardar token tras registro exitoso para evitar logout inmediato en iOS
                    authLocalDataSource.saveToken("mock_token_${userDto.id}")
                    Result.success(localUser.toDomain())
                } catch (e: Exception) {
                    // El mensaje técnico se conserva en el error pero la UI muestra un texto genérico.
                    Result.failure(AppError.UnknownError(e.message))
                }
            },
            onFailure = {
                val mappedError = if (it is AppError) it else AppError.UnknownError(it.message)
                Result.failure(mappedError)
            }
        )
    }

    override suspend fun getCurrentUser(): Result<User?> {
        val userId = authLocalDataSource.getCurrentUserId() ?: return Result.success(null)
        return try {
            Result.success(userDao.getUserById(userId)?.toDomain())
        } catch (e: Exception) {
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun restoreSession(): Result<User?> {
        if (!authLocalDataSource.isRememberMe() || authLocalDataSource.getToken() == null) return Result.success(null)
        return getCurrentUser()
    }

    override suspend fun getSessionUserId(): String? {
        return authLocalDataSource.getCurrentUserId()?.toString()
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return authRemoteDataSource.forgotPassword(email.normalizeEmail())
    }

    override suspend fun logout(): Result<Unit> {
        authLocalDataSource.clearSession()
        return Result.success(Unit)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val user = try {
            authLocalDataSource.getCurrentUserId()?.let { userDao.getUserById(it) }
        } catch (e: Exception) {
            return Result.failure(AppError.UnknownError(e.message))
        } ?: return Result.failure(AppError.Unauthorized())

        return authRemoteDataSource.deleteAccount(user.email).fold(
            onSuccess = {
                try {
                    // Aquí sí se quiere el borrado en cascada: sólo los medicamentos de esta cuenta.
                    userDao.deleteUserById(user.id)
                    authLocalDataSource.clearSession()
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(AppError.UnknownError(e.message))
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun String.normalizeEmail(): String = trim().lowercase()
}
