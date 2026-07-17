package com.connectapp.presentation.home

interface HomeEffect {
    data class OnShowToast(val item: DashboardItem) : HomeEffect
    data object NavigateToAddMedication : HomeEffect
}