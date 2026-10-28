package com.connectapp.domain.usecase

/** Legacy entry point; all reminders are rebuilt from persisted dose events. */
class SaveNotificationUseCase(private val syncReminders: SyncRemindersUseCase) {
    suspend operator fun invoke(medication: com.connectapp.domain.model.Medication) = syncReminders()
}
