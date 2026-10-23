package com.connectapp.presentation.theme

import com.connectapp.domain.manager.PreferenceStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeController(
    private val preferenceStorage: PreferenceStorage
) {

    companion object {
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }

    private val _isDarkTheme = MutableStateFlow(preferenceStorage.getBoolean(KEY_DARK_MODE, false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        preferenceStorage.putBoolean(KEY_DARK_MODE, enabled)
        _isDarkTheme.value = enabled
    }
}
