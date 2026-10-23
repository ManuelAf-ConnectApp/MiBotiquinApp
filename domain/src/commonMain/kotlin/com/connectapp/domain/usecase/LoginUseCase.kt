package com.connectapp.domain.usecase

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository,
    private val syncReminders: SyncRemindersUseCase,
) {
    suspend operator fun invoke(email: String, password: String, rememberMe: Boolean = false): Result<User> {
        return repository.login(email, password, rememberMe).onSuccess { syncReminders() }
    }
}
