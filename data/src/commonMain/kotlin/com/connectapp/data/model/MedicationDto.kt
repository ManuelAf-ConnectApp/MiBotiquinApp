package com.connectapp.data.model

data class MedicationDto(
    val id: Long = 0,
    val name: String,
    val dosesPerDay: Int,
    val quantity: Double,
    val quantityUnit: String,
    val durationDays: Int,
    val startDate: Long?,
    val endDate: Long?,
    val reminderMinutes: Int,
    val startTimeHour: Int,
    val startTimeMinute: Int,
    val codUser: String,
)
