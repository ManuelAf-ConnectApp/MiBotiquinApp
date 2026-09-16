package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository

class DeleteAccountUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.deleteAccount()
    }
}
