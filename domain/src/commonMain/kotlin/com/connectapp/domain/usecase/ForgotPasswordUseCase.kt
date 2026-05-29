package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository

class ForgotPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.forgotPassword(email)
    }
}
