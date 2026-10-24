package com.connectapp.domain.validator

import com.connectapp.domain.model.Medication

/** Calendar dates from Material DatePicker, encoded as UTC midnight. */
object MedicationRules {
    private const val DAY_MILLIS = 86_400_000L

    fun inclusiveDuration(start: Long?, end: Long?): Int? {
        if (start == null || end == null || end < start) return null
        if (start % DAY_MILLIS != 0L || end % DAY_MILLIS != 0L) return null
        val days = end / DAY_MILLIS - start / DAY_MILLIS + 1
        return days.takeIf { it in 1..Int.MAX_VALUE.toLong() }?.toInt()
    }

    fun isValid(medication: Medication): Boolean = with(medication) {
        name.isNotBlank() && codUser.isNotBlank() && doseQuantity != null && validCatalogue(medication) &&
            dosesPerDay in 1..10 && startTimeHour in 0..23 && startTimeMinute in 0..59 &&
            reminderMinutes in 0..1440 && startDate != null && startDate % DAY_MILLIS == 0L &&
            (if (isIndefinite) endDate == null && durationDays == 0
            else inclusiveDuration(startDate, endDate)?.let { it == durationDays } == true)
    }
    private fun validCatalogue(medication: Medication) = with(medication) {
        if (catalogueCode == null) catalogueName == null && catalogueRetrievedAt == null
        else catalogueCode.matches(Regex("[0-9]{6}")) && catalogueName == name && (catalogueRetrievedAt ?: 0) > 0
    }

}
