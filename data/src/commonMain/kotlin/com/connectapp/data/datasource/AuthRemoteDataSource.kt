package com.connectapp.data.datasource

import com.connectapp.data.interceptor.NetworkInterceptor
import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.database.entity.UserEntity
import com.connectapp.data.mapper.toDto
import com.connectapp.data.model.UserDto
import com.connectapp.data.security.PasswordHasher
import com.connectapp.domain.model.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/**
 * Local authentication store retained behind the former data-source contract so callers do not
 * depend on Room. It never contacts a service or sends recovery email.
 */
interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<UserDto>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun deleteAccount(email: String): Result<Unit>
}

class AuthRemoteDataSourceImpl(
    @Suppress("unused") private val networkInterceptor: NetworkInterceptor,
    private val users: UserDao
) : AuthRemoteDataSource {
    override suspend fun login(email: String, password: String): Result<UserDto> {
        val user = users.getUserByEmail(email) ?: return Result.failure(AppError.InvalidCredentials())
        val valid = withContext(Dispatchers.Default) { PasswordHasher.verify(password, user.password) }
        if (!valid) return Result.failure(AppError.InvalidCredentials())
        if (PasswordHasher.needsRehash(user.password)) {
            val replacement = withContext(Dispatchers.Default) { PasswordHasher.hash(password) }
            users.upgradePasswordHash(user.id, user.password, replacement)
        }
        return Result.success(user.toDto())
    }

    override suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<UserDto> {
        if (users.getUserByEmail(email) != null) return Result.failure(AppError.EmailAlreadyRegistered())
        return try {
            val now = Clock.System.now().toString()
            val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password) }
            val id = users.insertUser(UserEntity(
                firstName = firstName.trim(), lastName = lastName.trim(), email = email,
                password = hash, createdAt = now, updatedAt = now
            ))
            Result.success(checkNotNull(users.getUserById(id)).toDto())
        } catch (_: Exception) {
            // A competing registration is still reported as an existing local account.
            Result.failure(AppError.EmailAlreadyRegistered())
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return Result.failure(AppError.RecoveryUnavailable())
    }

    override suspend fun deleteAccount(email: String): Result<Unit> {
        return Result.success(Unit)
    }
}
