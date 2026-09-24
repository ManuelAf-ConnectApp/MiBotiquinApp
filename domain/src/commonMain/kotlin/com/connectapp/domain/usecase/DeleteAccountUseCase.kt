package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository

class DeleteAccountUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.deleteAccount(email)
    }
}
