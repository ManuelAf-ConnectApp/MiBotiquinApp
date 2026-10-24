package com.connectapp.domain.repository

interface RecoveryRepository {
    /** Reauthenticates the current local account and replaces any previous code. */
    suspend fun issueCode(currentPassword: String): Result<String>
    /** Consumes a code once, replaces the password and ends the current local session. */
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>
}
