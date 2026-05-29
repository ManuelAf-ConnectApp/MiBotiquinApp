package com.connectapp.presentation.settings

data class SettingsState(
    val isLoading: Boolean = false,
    val userName: String = "Android developer",
    val userEmail: String = "developer@kmp.com",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val appVersion: String = ""
)
