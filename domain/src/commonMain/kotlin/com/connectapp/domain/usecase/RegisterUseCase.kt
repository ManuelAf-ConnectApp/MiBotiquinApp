package com.connectapp.domain.usecase

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository,
    private val syncReminders: SyncRemindersUseCase
) {
    suspend operator fun invoke(firstName: String, lastName: String, email: String, password: String): Result<User> {
        return repository.register(firstName, lastName, email, password).onSuccess { syncReminders() }
    }
}
