package com.connectapp.data.datasource

import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.interceptor.NetworkInterceptor
import com.connectapp.data.mapper.toDto
import com.connectapp.data.model.UserDto
import com.connectapp.data.security.PasswordHasher
import com.connectapp.domain.model.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

// Updated AuthRemoteDataSource
interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<UserDto>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun deleteAccount(email: String): Result<Unit>
}

// Simulación de backend mientras no exista un servidor real: las cuentas se validan contra la
// tabla users local, donde la contraseña se guarda como hash PBKDF2 (nunca en claro).
// Al conectar un backend, esta clase se sustituye por las llamadas HTTP equivalentes.
class AuthRemoteDataSourceImpl(
    private val networkInterceptor: NetworkInterceptor,
    private val userDao: UserDao
) : AuthRemoteDataSource {

    override suspend fun login(email: String, password: String): Result<UserDto> {
        return networkInterceptor.execute {
            val user = userDao.getUserByEmail(email)
            // Mismo error si no existe la cuenta o si la contraseña no coincide, para no
            // revelar qué emails están registrados.
            val valid = user != null && withContext(Dispatchers.Default) {
                PasswordHasher.verify(password, user.password)
            }
            if (user == null || !valid) throw AppError.InvalidCredentials()

            if (PasswordHasher.needsRehash(user.password)) {
                val rehashed = withContext(Dispatchers.Default) { PasswordHasher.hash(password) }
                userDao.updateUser(user.copy(password = rehashed))
            }
            user.toDto()
        }
    }

    override suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<UserDto> {
        return networkInterceptor.execute {
            if (userDao.getUserByEmail(email) != null) throw AppError.EmailAlreadyRegistered()

            val now = Clock.System.now().toString()
            UserDto(
                id = "0", // la base de datos local asigna el id definitivo
                email = email,
                firstName = firstName,
                lastName = lastName,
                password = withContext(Dispatchers.Default) { PasswordHasher.hash(password) },
                createdAt = now,
                updatedAt = now
            )
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return networkInterceptor.execute { }
    }

    override suspend fun deleteAccount(email: String): Result<Unit> {
        // La cuenta vive en la BD local: AuthRepositoryImpl la borra al recibir el éxito.
        return networkInterceptor.execute { }
    }
}
