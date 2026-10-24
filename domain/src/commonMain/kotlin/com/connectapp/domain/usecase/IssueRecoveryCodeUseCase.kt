package com.connectapp.domain.usecase

import com.connectapp.domain.repository.RecoveryRepository

class IssueRecoveryCodeUseCase(private val repository: RecoveryRepository) {
    suspend operator fun invoke(currentPassword: String): Result<String> = repository.issueCode(currentPassword)
}
