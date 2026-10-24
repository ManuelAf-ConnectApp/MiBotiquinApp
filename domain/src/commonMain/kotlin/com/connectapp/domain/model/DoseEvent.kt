package com.connectapp.domain.model

enum class DoseStatus { PENDING, TAKEN, SKIPPED, OBSOLETE }

data class DoseReminder(
    val eventId: String,
    val medicationId: Long,
    val scheduledAt: Long,
    val reminderAt: Long
)

data class DoseDetail(
    val eventId: String,
    val medicationId: Long,
    val medicationName: String,
    val quantity: DoseQuantity,
    val scheduledAt: Long,
    val reminderAt: Long,
    val status: DoseStatus,
    val resolvedAt: Long?,
    val notes: String,
    val pharmacologicalDose: String,
    val stockTracked: Boolean = false,
    val stockAvailableMinor: Long? = null
)
