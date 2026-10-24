package com.connectapp.domain.usecase

import com.connectapp.domain.model.AppError
import com.connectapp.domain.repository.RecoveryRepository
import com.connectapp.domain.validator.FormValidator

class ResetPasswordUseCase(
    private val repository: RecoveryRepository,
    private val validator: FormValidator,
    private val syncReminders: SyncRemindersUseCase
) {
    suspend operator fun invoke(email: String, code: String, password: String): Result<Unit> {
        if (!validator.isValidEmail(email) || !validator.isValidPassword(password) || code.isBlank()) {
            return Result.failure(AppError.InvalidRecoveryCredentials())
        }
        return repository.resetPassword(email, code, password).onSuccess { syncReminders() }
    }
}
