package com.connectapp.domain.usecase

import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.manager.PreferenceKeys
import com.connectapp.domain.manager.PreferenceStorage
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.repository.MedicationRepository

// Única fuente de verdad de qué recordatorios deben estar programados: los medicamentos del
// usuario con sesión iniciada, sólo si tiene las notificaciones activadas. Se invoca tras
// cualquier cambio que afecte a ese conjunto (login, logout, guardar/borrar medicamento,
// activar/desactivar notificaciones) y al arrancar la app o el dispositivo.
class SyncRemindersUseCase(
    private val authRepository: AuthRepository,
    private val medicationRepository: MedicationRepository,
    private val notificationManager: NotificationManager,
    private val preferenceStorage: PreferenceStorage
) {
    suspend operator fun invoke() {
        val enabled = preferenceStorage.getBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, true)
        val userId = authRepository.getSessionUserId()

        val medications = if (enabled && userId != null) {
            // Si la lectura falla se dejan los recordatorios como estaban antes que borrarlos.
            medicationRepository.getUserMedicationsByCodUser(userId).getOrElse { return }
        } else {
            emptyList()
        }
        notificationManager.scheduleReminders(medications)
    }
}
