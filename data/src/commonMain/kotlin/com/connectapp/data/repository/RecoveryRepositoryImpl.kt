package com.connectapp.data.repository

import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.security.PasswordHasher
import com.connectapp.domain.model.AppError
import com.connectapp.domain.repository.RecoveryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RecoveryRepositoryImpl(
    private val users: UserDao,
    private val session: AuthLocalDataSource
) : RecoveryRepository {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun issueCode(currentPassword: String): Result<String> = safely {
        val userId = session.getCurrentUserId() ?: throw AppError.Unauthorized()
        val user = users.getUserById(userId) ?: throw AppError.Unauthorized()
        if (!withContext(Dispatchers.Default) { PasswordHasher.verify(currentPassword, user.password) }) {
            throw AppError.InvalidCredentials()
        }
        // UUID v4 uses the platform secure random source, as does PasswordHasher's salt.
        val code = Uuid.random().toString().replace("-", "").uppercase().chunked(4).joinToString("-")
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(normalizeCode(code)) }
        if (session.getCurrentUserId() != userId || users.setRecoveryHash(userId, user.password, hash) != 1) {
            throw AppError.Unauthorized()
        }
        code
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> = safely {
        val normalizedCode = normalizeCode(code)
        if (newPassword.length < 8 || !normalizedCode.matches(Regex("[0-9A-F]{32}"))) {
            throw AppError.InvalidRecoveryCredentials()
        }
        val user = users.getUserByEmail(email.trim().lowercase())
        val storedHash = user?.recoveryHash
        // Run a KDF even for an unknown account or an account without an enrolled code.
        val valid = withContext(Dispatchers.Default) {
            if (storedHash == null) {
                PasswordHasher.hash(normalizedCode)
                false
            } else PasswordHasher.verify(normalizedCode, storedHash)
        }
        if (user == null || storedHash == null || !valid) throw AppError.InvalidRecoveryCredentials()
        val passwordHash = withContext(Dispatchers.Default) { PasswordHasher.hash(newPassword) }
        if (users.consumeRecoveryCode(user.id, storedHash, user.password, passwordHash, Clock.System.now().toString()) != 1) {
            throw AppError.InvalidRecoveryCredentials()
        }
        session.clearSession()
    }

    private fun normalizeCode(code: String) = code.trim().replace("-", "").uppercase()

    private suspend fun <T> safely(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (e: CancellationException) {
        throw e
    } catch (e: AppError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AppError.UnknownError())
    }
}
