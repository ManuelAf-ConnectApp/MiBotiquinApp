package com.connectapp.domain.model

data class NotificationDiagnostics(
    val systemEnabled: Boolean,
    val exactAlarmsAvailable: Boolean?,
    val scheduledCount: Int,
    val detail: String
)
