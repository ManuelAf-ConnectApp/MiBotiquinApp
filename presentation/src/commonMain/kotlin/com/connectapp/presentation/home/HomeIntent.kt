package com.connectapp.presentation.home

import com.connectapp.domain.model.User

sealed interface HomeIntent {
    data class ItemClicked(val item: DashboardItem) : HomeIntent
    data object AddMedicationClicked : HomeIntent

    data class InitData(val user: User): HomeIntent
}