package com.connectapp.domain.manager

import com.connectapp.domain.model.DoseReminder
import com.connectapp.domain.model.NotificationDiagnostics

interface NotificationManager {
    fun showNotification(title: String, body: String)

    /**
     * Sustituye todos los recordatorios programados por la app por los de [reminders]:
     * los medicamentos que no estén en la lista (borrados, de otro usuario...) dejan de sonar.
     * Una lista vacía cancela todos los recordatorios.
     */
    fun scheduleReminders(reminders: List<DoseReminder>)
    fun diagnostics(): NotificationDiagnostics
}
