package com.connectapp.presentation.settings

import com.connectapp.presentation.util.UiText

data class SettingsState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val appVersion: String = "—",
    val notificationDiagnostic: String? = null,
    val errorMessage: UiText? = null
)
