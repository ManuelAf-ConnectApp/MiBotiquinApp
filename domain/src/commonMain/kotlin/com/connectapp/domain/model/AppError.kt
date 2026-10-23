package com.connectapp.domain.model

sealed class AppError : Throwable() {
    class NetworkError(override val message: String? = null) : AppError()
    class Unauthorized(override val message: String? = null) : AppError()
    class ServerError(override val message: String? = null) : AppError()
    class UserNotFound(override val message: String? = null) : AppError()
    class InvalidCredentials(override val message: String? = null) : AppError()
    class EmailAlreadyRegistered(override val message: String? = null) : AppError()
    class UnknownError(override val message: String? = null) : AppError()
    data class CustomError(override val message: String) : AppError()
}
