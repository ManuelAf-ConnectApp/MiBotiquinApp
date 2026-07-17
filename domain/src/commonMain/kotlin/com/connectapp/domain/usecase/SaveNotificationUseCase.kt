package com.connectapp.domain.usecase

import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.model.Medication

class SaveNotificationUseCase(
    private val notificationManager: NotificationManager
) {
    operator fun invoke(medication: Medication) {
        notificationManager.scheduleNotification(medication)
    }
}