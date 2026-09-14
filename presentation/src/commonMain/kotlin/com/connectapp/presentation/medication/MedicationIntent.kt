package com.connectapp.presentation.medication

import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User

sealed interface MedicationIntent {

    data class OnInitUserData(val user: User) : MedicationIntent
    data class LoadMedication(val medication: Medication) : MedicationIntent
    data object ClearMedication : MedicationIntent
    data class NameChanged(val name: String) : MedicationIntent
    data class DosesPerDayChanged(val doses: Int) : MedicationIntent
    data class QuantityChanged(val quantity: String) : MedicationIntent
    data class QuantityUnitChanged(val unit: String) : MedicationIntent
    data class StartDateChanged(val date: Long?) : MedicationIntent
    data class EndDateChanged(val date: Long?) : MedicationIntent
    data class ReminderMinutesChanged(val minutes: Int) : MedicationIntent
    data class StartTimeChanged(val hour: Int, val minute: Int) : MedicationIntent
    data object SaveClicked : MedicationIntent
}
