package com.connectapp.presentation.settings

import com.connectapp.domain.model.AppInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.default_user_name
import com.connectapp.domain.usecase.GetCurrentUserUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.SyncRemindersUseCase
import com.connectapp.domain.manager.NotificationManager
import com.connectapp.presentation.notification.NotificationsController
import com.connectapp.presentation.theme.ThemeController
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class SettingsViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val notificationsController: NotificationsController,
    private val themeController: ThemeController,
    private val syncReminders: SyncRemindersUseCase,
    private val notificationManager: NotificationManager,
    appInfo: AppInfo
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState(
        appVersion = appInfo.version,
        notificationsEnabled = notificationsController.notificationsEnabled.value,
        darkModeEnabled = themeController.isDarkTheme.value
    ))
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    init {
        observeTheme()
        observeNotifications()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeController.isDarkTheme.collect { isDark ->
                _state.update { it.copy(darkModeEnabled = isDark) }
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationsController.notificationsEnabled.collect { enabled ->
                _state.update { it.copy(notificationsEnabled = enabled) }
            }
        }
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getCurrentUserUseCase().onSuccess { user ->
                val userName = user?.let { "${it.firstName} ${it.lastName}" } ?: getString(default_user_name)
                _state.update {
                    it.copy(
                        isLoading = false,
                        userName = userName,
                        userEmail = user?.email ?: ""
                    )
                }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.asUiText()
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.NotificationDiagnosticsClicked -> {
                val diagnostic = notificationManager.diagnostics()
                _state.update { it.copy(notificationDiagnostic = "${diagnostic.detail}: sistema=" + if (diagnostic.systemEnabled) "activo" else "bloqueado" + ", alarmas exactas=" + (diagnostic.exactAlarmsAvailable?.let { if (it) "disponibles" else "no disponibles" } ?: "no aplicable") + ", avisos planificados=${diagnostic.scheduledCount}") }
            }
            SettingsIntent.LoadSettings -> loadUserSettings()
            SettingsIntent.LogoutClicked -> logout()
            is SettingsIntent.NotificationsToggled -> {
                notificationsController.setNotificationsEnabled(intent.enabled)
                viewModelScope.launch { syncReminders() }
            }

            is SettingsIntent.DarkModeToggled -> {
                themeController.setDarkTheme(intent.enabled)
            }

            SettingsIntent.EditProfileClicked -> {
                viewModelScope.launch {
                    _effect.emit(SettingsEffect.NavigateToProfile)
                }
            }

            SettingsIntent.SecurityClicked -> {
                viewModelScope.launch {
                    _effect.emit(SettingsEffect.NavigateToProfile)
                }
            }

            SettingsIntent.PrivacyPolicyClicked -> {
                viewModelScope.launch {
                    _effect.emit(SettingsEffect.ShowPrivacyInfo)
                }
            }
        }
    }

    private fun logout() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            logoutUseCase().onSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(SettingsEffect.NavigateToLogin)
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.asUiText()
                    )
                }
            }
        }
    }
}
