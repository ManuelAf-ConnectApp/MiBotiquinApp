package com.connectapp.domain.manager

import com.connectapp.domain.model.Medication

interface NotificationManager {
    fun showNotification(title: String, body: String)

    fun scheduleNotification(medication: Medication)

    fun cancelNotification(medication: Medication)
}