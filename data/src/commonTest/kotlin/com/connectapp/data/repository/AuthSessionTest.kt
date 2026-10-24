package com.connectapp.data.repository

import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.database.entity.UserEntity
import com.connectapp.data.datasource.*
import com.connectapp.data.interceptor.AuthInterceptor
import com.connectapp.data.mapper.toDto
import com.connectapp.data.model.UserDto
import com.connectapp.domain.model.AppError
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class AuthSessionTest {
    private class Local : AuthLocalDataSource {
        var storedToken: String? = "old-token"
        var currentId: Long? = 1
        var remember = true
        override fun saveToken(token: String) { storedToken = token }
        override fun getToken() = storedToken
        override fun saveCurrentUserId(id: Long) { currentId = id }
        override fun getCurrentUserId() = currentId
        override fun clearSession() { storedToken = null; currentId = null; remember = false }
        override fun saveRememberMe(remember: Boolean) { this.remember = remember }
        override fun isRememberMe() = remember
    }

    private class Users : UserDao {
        val user = UserEntity(2, "Test", "User", "test@example.com", "unused", "", "")
        override suspend fun getUserById(id: Long) = user.takeIf { it.id == id }
        override suspend fun getUserByEmail(email: String) = user.takeIf { it.email == email }
        override fun observeUserById(id: Long) = flowOf(user.takeIf { it.id == id })
        override suspend fun insertUser(user: UserEntity) = error("Unexpected insert")
        override suspend fun updateUser(user: UserEntity) = error("Unexpected update")
        override suspend fun upgradePasswordHash(id: Long, expectedPassword: String, passwordHash: String): Int = error("Unused")
        override suspend fun setRecoveryHash(id: Long, expectedPassword: String, recoveryHash: String): Int = error("Unused")
        override suspend fun consumeRecoveryCode(id: Long, expectedRecoveryHash: String, expectedPassword: String, passwordHash: String, updatedAt: String): Int = error("Unused")
        override suspend fun deleteUserById(id: Long) = error("Unexpected delete")
    }

    private class Login(private val users: Users) : AuthRemoteDataSource {
        override suspend fun login(email: String, password: String) = Result.success(users.user.toDto())
        override suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<UserDto> = error("Unused")
        override suspend fun forgotPassword(email: String): Result<Unit> = error("Unused")
        override suspend fun deleteAccount(email: String): Result<Unit> = error("Unused")
    }

    @Test fun loginWithoutRememberMeReplacesOldPersistentSession() = runBlocking<Unit> {
        val local = Local()
        val users = Users()
        val repository = AuthRepositoryImpl(local, Login(users), users)
        assertTrue(repository.login("test@example.com", "unused", false).isSuccess)
        assertEquals("2", repository.getSessionUserId())
        assertEquals("2", repository.getCurrentUser().getOrThrow()?.id)
        assertNull(local.storedToken)
        assertFalse(local.remember)
        assertNull(repository.restoreSession().getOrThrow())
    }

    @Test fun rememberedLoginRestoresAndLogoutClearsSession() = runBlocking<Unit> {
        val local = Local()
        val users = Users()
        val repository = AuthRepositoryImpl(local, Login(users), users)
        assertTrue(repository.login("test@example.com", "unused", true).isSuccess)
        assertEquals("2", repository.restoreSession().getOrThrow()?.id)
        repository.logout().getOrThrow()
        assertNull(repository.getSessionUserId())
        assertNull(repository.restoreSession().getOrThrow())
    }

    @Test fun recoveryNeverClaimsToSendEmailOrChangesCredentials() = runBlocking<Unit> {
        val local = Local()
        val users = Users()
        val source = AuthRemoteDataSourceImpl(AuthInterceptor(local), users)
        for (email in listOf("test@example.com", "unknown@example.com")) {
            assertIs<AppError.RecoveryUnavailable>(source.forgotPassword(email).exceptionOrNull())
        }
        assertEquals("unused", users.user.password)
        assertEquals(1L, local.currentId)
    }
}
