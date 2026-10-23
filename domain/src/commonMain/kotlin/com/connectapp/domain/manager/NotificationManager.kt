package com.connectapp.domain.manager

import com.connectapp.domain.model.Medication

interface NotificationManager {
    fun showNotification(title: String, body: String)

    /**
     * Sustituye todos los recordatorios programados por la app por los de [medications]:
     * los medicamentos que no estén en la lista (borrados, de otro usuario...) dejan de sonar.
     * Una lista vacía cancela todos los recordatorios.
     */
    fun scheduleReminders(medications: List<Medication>)
}
