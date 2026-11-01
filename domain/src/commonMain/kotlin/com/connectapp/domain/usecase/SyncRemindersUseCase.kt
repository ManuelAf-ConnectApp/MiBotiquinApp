package com.connectapp.domain.usecase

import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.manager.PreferenceKeys
import com.connectapp.domain.manager.PreferenceStorage
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.repository.DoseRepository
import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Única fuente de verdad de qué recordatorios deben estar programados: los medicamentos del
// usuario con sesión iniciada, sólo si tiene las notificaciones activadas. Se invoca tras
// cualquier cambio que afecte a ese conjunto (login, logout, guardar/borrar medicamento,
// activar/desactivar notificaciones) y al arrancar la app o el dispositivo.
class SyncRemindersUseCase(
    private val authRepository: AuthRepository,
    private val doseRepository: DoseRepository,
    private val notificationManager: NotificationManager,
    private val preferenceStorage: PreferenceStorage
) {
    private val mutex = Mutex()

    suspend operator fun invoke() = mutex.withLock {
        val enabled = preferenceStorage.getBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, true)
        val userId = authRepository.getSessionUserId()
        val reminders = if (enabled && userId != null) {
            doseRepository.planReminders(Clock.System.now().toEpochMilliseconds()).getOrNull()
        } else emptyList()
        // Do not schedule results belonging to a session that changed while planning.
        if (authRepository.getSessionUserId() != userId) return@withLock
        // Planning can suspend while the user disables notifications. Re-read the
        // preference even after a planning failure so cancellation is never lost.
        if (!preferenceStorage.getBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, true) || userId == null) {
            notificationManager.scheduleReminders(emptyList())
        } else if (reminders != null) {
            notificationManager.scheduleReminders(reminders)
        }
    }
}
