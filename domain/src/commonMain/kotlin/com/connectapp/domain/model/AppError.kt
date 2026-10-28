package com.connectapp.domain.model

sealed class AppError : Throwable() {
    class InvalidMedicationCode : AppError()
    class CatalogueUnavailable : AppError()
    class InvalidStock : AppError()
    class InsufficientStock : AppError()
    class StockIdentityChange : AppError()
    class StockUnitMismatch : AppError()
    class InvalidDose : AppError()
    class InvalidMedication : AppError()
    class InvalidRecoveryCredentials : AppError()
    class RecoveryUnavailable : AppError()
    class NetworkError(override val message: String? = null) : AppError()
    class Unauthorized(override val message: String? = null) : AppError()
    class ServerError(override val message: String? = null) : AppError()
    class UserNotFound(override val message: String? = null) : AppError()
    class InvalidCredentials(override val message: String? = null) : AppError()
    class EmailAlreadyRegistered(override val message: String? = null) : AppError()
    class UnknownError(override val message: String? = null) : AppError()
    data class CustomError(override val message: String) : AppError()
}
