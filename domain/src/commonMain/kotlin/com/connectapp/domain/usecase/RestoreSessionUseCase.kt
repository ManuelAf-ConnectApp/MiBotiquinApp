package com.connectapp.domain.usecase

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

class RestoreSessionUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<User?> {
        return repository.restoreSession()
    }
}
