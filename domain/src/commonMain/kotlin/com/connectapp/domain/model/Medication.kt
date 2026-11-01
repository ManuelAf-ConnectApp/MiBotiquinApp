package com.connectapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
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
    val startTimeMinute: Int,
    val quantityMinor: Long? = null,
    val isIndefinite: Boolean = false,
    val isPaused: Boolean = false,
    val notes: String = "",
    val pharmacologicalDose: String = "",
    val catalogueCode: String? = null,
    val catalogueName: String? = null,
    val catalogueRetrievedAt: Long? = null,
    val customSchedule: String = "",
    /** ISO weekday overrides: `1=08:00|20:00;3=09:00` (1 Monday, 7 Sunday). */
    val weekdaySchedule: String = "",
    val isArchived: Boolean = false
) {
    // Legacy Double is retained for compatibility; all new quantities use integer thousandths.
    val doseQuantity: DoseQuantity?
        get() {
            val unit = DoseUnit.fromStored(quantityUnit) ?: return null
            return quantityMinor?.let { DoseQuantity.fromMinorUnits(it, unit) }
                ?: if (quantityMinor == null) DoseQuantity.parse(quantity.toString(), unit) else null
        }

    val intervalHours: Int
        get() = if (dosesPerDay > 0) 24 / dosesPerDay else 24
}
