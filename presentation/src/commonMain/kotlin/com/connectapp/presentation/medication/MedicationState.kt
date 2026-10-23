package com.connectapp.presentation.medication

import com.connectapp.domain.model.User
import com.connectapp.presentation.util.UiText

data class MedicationState(
    val id: Long = 0,
    val user: User? = null,
    val name: String = "",
    val dosesPerDay: Int = 1,
    val quantity: String = "",
    val quantityUnit: String = "Pastillas", // Default unit
    val durationDays: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val reminderMinutes: Int = 10,
    val startTimeHour: Int = 8,
    val startTimeMinute: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val isSaveButtonEnabled: Boolean = user != null && name.isNotBlank() && dosesPerDay > 0 && parsedQuantity != null && durationDays.isNotEmpty() && startTimeHour in 0..23 && startTimeMinute in 0..59
}

// Acepta coma decimal ("1,5"); null si no es un número positivo, para no guardar 0.
val MedicationState.parsedQuantity: Double?
    get() = quantity.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 && it.isFinite() }
