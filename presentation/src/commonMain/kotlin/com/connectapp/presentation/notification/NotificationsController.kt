package com.connectapp.presentation.notification

import com.connectapp.domain.manager.PreferenceKeys
import com.connectapp.domain.manager.PreferenceStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Single Koin instance shared by every screen that reads/toggles the notifications
// preference (TopBar bell, Settings switch): a per-ViewModel StateFlow would drift out
// of sync as soon as the preference changed from a different screen. Mirrors ThemeController.
class NotificationsController(
    private val preferenceStorage: PreferenceStorage
) {
    private val _notificationsEnabled = MutableStateFlow(
        preferenceStorage.getBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        preferenceStorage.putBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, enabled)
        _notificationsEnabled.value = enabled
    }

    fun toggle() {
        setNotificationsEnabled(!_notificationsEnabled.value)
    }
}
