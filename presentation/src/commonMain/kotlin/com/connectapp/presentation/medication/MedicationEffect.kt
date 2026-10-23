package com.connectapp.presentation.medication

sealed interface MedicationEffect {
    data class ShowToast(val message: String) : MedicationEffect

    object NavigateBack : MedicationEffect
}