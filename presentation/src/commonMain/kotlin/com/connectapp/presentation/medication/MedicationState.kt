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
    val isSaveButtonEnabled: Boolean = user != null && name.isNotEmpty() && dosesPerDay > 0 && quantity.isNotEmpty() && durationDays.isNotEmpty() && startTimeHour in 0..23 && startTimeMinute in 0..59
}
