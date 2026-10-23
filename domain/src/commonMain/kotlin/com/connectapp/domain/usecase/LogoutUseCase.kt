package com.connectapp.domain.usecase

import com.connectapp.domain.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository,
    private val syncReminders: SyncRemindersUseCase
) {
    suspend operator fun invoke(): Result<Unit> {
        // Sin sesión, la sincronización cancela todos los recordatorios.
        return repository.logout().onSuccess { syncReminders() }
    }
}
