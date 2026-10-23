package com.connectapp.presentation.home

import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User

sealed interface HomeIntent {
    data object AddMedicationClicked : HomeIntent
    data class MedicationClicked(val medication: Medication) : HomeIntent
    data class DeleteMedicationConfirmed(val medication: Medication) : HomeIntent

    data class InitData(val user: User): HomeIntent
}