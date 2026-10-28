package com.connectapp.domain.repository

import com.connectapp.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String, rememberMe: Boolean): Result<User>
    suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<User>
    suspend fun forgotPassword(email: String): Result<Unit>
    // Usuario con sesión iniciada, haya marcado o no "Recordarme".
    suspend fun getCurrentUser(): Result<User?>
    // Usuario cuya sesión debe restaurarse al abrir la app: sólo si marcó "Recordarme".
    suspend fun restoreSession(): Result<User?>
    // Id del usuario con sesión iniciada aunque no marcase "Recordarme" (los recordatorios
    // deben seguir sonando); null tras cerrar sesión o borrar la cuenta.
    suspend fun getSessionUserId(): String?
    suspend fun logout(): Result<Unit>
    // Borra la cuenta de la sesión actual (nunca la de un email recibido desde la UI).
    suspend fun deleteAccount(): Result<Unit>
}
