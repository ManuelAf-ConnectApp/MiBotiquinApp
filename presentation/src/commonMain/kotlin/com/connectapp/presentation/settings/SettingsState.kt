package com.connectapp.presentation.settings

import com.connectapp.presentation.util.UiText

data class SettingsState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val appVersion: String = "1.0.1",
    val errorMessage: UiText? = null
)
