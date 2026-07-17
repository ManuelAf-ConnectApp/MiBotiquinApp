package com.connectapp.domain.model

data class Medication(
    val id: Long = 0,
    val name: String,
    val codUser: String,
    val dosesPerDay: Int,
    val quantity: Double,
    val quantityUnit: String,
    val durationDays: Int,
    val startDate: Long?,
    val endDate: Long?,
    val reminderMinutes: Int,
    val startTimeHour: Int,
    val startTimeMinute: Int
) {
    val intervalHours: Int
        get() = if (dosesPerDay > 0) 24 / dosesPerDay else 24
}
