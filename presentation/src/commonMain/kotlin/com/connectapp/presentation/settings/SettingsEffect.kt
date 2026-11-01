package com.connectapp.presentation.settings

sealed interface SettingsEffect {
    data object ShowPrivacyInfo : SettingsEffect
    data object NavigateToLogin : SettingsEffect
    data object NavigateToProfile : SettingsEffect
    data class ShowMessage(val message: String) : SettingsEffect
}
