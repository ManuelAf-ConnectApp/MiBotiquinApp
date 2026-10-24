package com.connectapp.data.repository

import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.database.entity.UserEntity
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.security.PasswordHasher
import com.connectapp.domain.model.AppError
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class RecoveryRepositoryTest {
    private class Session : AuthLocalDataSource {
        var id: Long? = 1L
        override fun getCurrentUserId() = id
        override fun saveCurrentUserId(id: Long) { this.id = id }
        override fun clearSession() { id = null }
        override fun getToken(): String? = null
        override fun saveToken(token: String) = Unit
        override fun saveRememberMe(remember: Boolean) = Unit
        override fun isRememberMe() = false
    }

    private class Users : UserDao {
        var row = UserEntity(1, "Test", "User", "test@example.com", PasswordHasher.hash("old-password"), "", "")
        var rejectConsume = false
        override suspend fun getUserById(id: Long) = row.takeIf { it.id == id }
        override suspend fun getUserByEmail(email: String) = row.takeIf { it.email == email }
        override fun observeUserById(id: Long) = flowOf(row.takeIf { it.id == id })
        override suspend fun upgradePasswordHash(id: Long, expectedPassword: String, passwordHash: String): Int = error("Unused")
        override suspend fun setRecoveryHash(id: Long, expectedPassword: String, recoveryHash: String): Int {
            if (row.id != id || row.password != expectedPassword) return 0
            row = row.copy(recoveryHash = recoveryHash)
            return 1
        }
        override suspend fun consumeRecoveryCode(id: Long, expectedRecoveryHash: String, expectedPassword: String, passwordHash: String, updatedAt: String): Int {
            if (rejectConsume || row.id != id || row.recoveryHash != expectedRecoveryHash || row.password != expectedPassword) return 0
            row = row.copy(password = passwordHash, recoveryHash = null, updatedAt = updatedAt)
            return 1
        }
        override suspend fun insertUser(user: UserEntity): Long = error("Unused")
        override suspend fun updateUser(user: UserEntity) { row = user }
        override suspend fun deleteUserById(id: Long) = error("Unused")
    }

    @Test fun issuanceRequiresSessionAndCurrentPasswordAndStoresOnlyHash() = runBlocking<Unit> {
        val users = Users()
        val session = Session()
        val repository = RecoveryRepositoryImpl(users, session)
        assertIs<AppError.InvalidCredentials>(repository.issueCode("wrong-password").exceptionOrNull())
        assertNull(users.row.recoveryHash)
        val code = repository.issueCode("old-password").getOrThrow()
        assertTrue(code.matches(Regex("[0-9A-F]{4}(-[0-9A-F]{4}){7}")))
        assertNotEquals(code, users.row.recoveryHash)
        assertTrue(PasswordHasher.verify(code.replace("-", ""), users.row.recoveryHash!!))
        session.id = null
        assertIs<AppError.Unauthorized>(repository.issueCode("old-password").exceptionOrNull())
    }

    @Test fun missingCodeAndWeakPasswordCannotChangeCredentials() = runBlocking<Unit> {
        val users = Users()
        val session = Session()
        val repository = RecoveryRepositoryImpl(users, session)
        val originalPassword = users.row.password
        assertIs<AppError.InvalidRecoveryCredentials>(repository.resetPassword("test@example.com", "", "new-password").exceptionOrNull())
        val code = repository.issueCode("old-password").getOrThrow()
        assertIs<AppError.InvalidRecoveryCredentials>(repository.resetPassword("test@example.com", code, "short").exceptionOrNull())
        assertEquals(originalPassword, users.row.password)
        assertNotNull(users.row.recoveryHash)
    }

    @Test fun successfulResetPreservesIdentityConsumesCodeAndEndsSession() = runBlocking<Unit> {
        val users = Users()
        val session = Session()
        val repository = RecoveryRepositoryImpl(users, session)
        val code = repository.issueCode("old-password").getOrThrow()
        repository.resetPassword(" TEST@example.com ", code.lowercase(), "new-password").getOrThrow()
        assertEquals(1L, users.row.id)
        assertEquals("test@example.com", users.row.email)
        assertTrue(PasswordHasher.verify("new-password", users.row.password))
        assertFalse(PasswordHasher.verify("old-password", users.row.password))
        assertNull(users.row.recoveryHash)
        assertNull(session.id)
        assertIs<AppError.InvalidRecoveryCredentials>(repository.resetPassword("test@example.com", code, "another-password").exceptionOrNull())
    }

    @Test fun wrongAccountRotatedCodeAndConcurrentConsumptionCannotResetPassword() = runBlocking<Unit> {
        val users = Users()
        val session = Session()
        val repository = RecoveryRepositoryImpl(users, session)
        val oldCode = repository.issueCode("old-password").getOrThrow()
        val code = repository.issueCode("old-password").getOrThrow()
        for ((email, attempt) in listOf("unknown@example.com" to code, "test@example.com" to oldCode)) {
            assertIs<AppError.InvalidRecoveryCredentials>(repository.resetPassword(email, attempt, "new-password").exceptionOrNull())
        }
        users.rejectConsume = true
        assertIs<AppError.InvalidRecoveryCredentials>(repository.resetPassword("test@example.com", code, "new-password").exceptionOrNull())
        assertTrue(PasswordHasher.verify("old-password", users.row.password))
        assertNotNull(users.row.recoveryHash)
        assertEquals(1L, session.id)
    }
}
