package com.connectapp.presentation.home

import com.connectapp.domain.model.Medication

interface HomeEffect {
    data object NavigateToAddMedication : HomeEffect
    data class NavigateToEditMedication(val medication: Medication) : HomeEffect
}