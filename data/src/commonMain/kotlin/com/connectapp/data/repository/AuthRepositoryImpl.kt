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
        password: String,
        rememberMe: Boolean
    ): Result<User> {
        if (authLocalDataSource.getToken() != null) {
            val user = userDao.getUserOnce()?.toDomain()
            if (user != null) {
                return Result.success(user)
            }
        }

        return authRemoteDataSource.login(email, password).map { userDto ->
            val userEntity = userDto.toEntity()
            // La tabla "users" solo debe contener al usuario de la sesión activa
            // (getUserOnce/getUser leen "LIMIT 1" sin WHERE); si no se limpia antes,
            // cuentas de logins anteriores (o el usuario semilla) quedan como filas
            // sueltas y esa lectura puede devolver la fila equivocada.
            // Si el usuario que inicia sesión ya es el que hay en local, no lo
            // borramos: "medications" tiene FK a "users" con onDelete=CASCADE, y
            // reinsertarlo en cada login (sin "recordar sesión") arrastraría sus
            // medicamentos ya guardados.
            if (userDao.getUserOnce()?.id != userEntity.id) {
                userDao.clearUser()
                userDao.insertUser(userEntity)
            }

            if (rememberMe) {
                authLocalDataSource.saveToken("mock_token_${userDto.id}")
                authLocalDataSource.saveRememberMe(true)
            }
            
            userDto.toDomain()
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
            userDao.clearUser()
            userDao.insertUser(userEntity)
            userDto.toDomain()
        }
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

}
