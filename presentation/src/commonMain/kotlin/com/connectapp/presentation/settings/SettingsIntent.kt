package com.connectapp.presentation.settings

sealed interface SettingsIntent {
    data object LoadSettings : SettingsIntent
    data object LogoutClicked : SettingsIntent
    data class NotificationsToggled(val enabled: Boolean) : SettingsIntent
    data class DarkModeToggled(val enabled: Boolean) : SettingsIntent
    data object EditProfileClicked : SettingsIntent
    data object SecurityClicked : SettingsIntent
    data object PrivacyPolicyClicked : SettingsIntent
}
