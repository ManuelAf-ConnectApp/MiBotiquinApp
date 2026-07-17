package com.connectapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.connectapp.domain.usecase.LogoutUseCase

class SettingsViewModel(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }

            // Simulate loading user settings
            delay(2_000)

            _state.update {
                it.copy(
                    isLoading = false,
                )
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.LogoutClicked -> logout()
            is SettingsIntent.NotificationsToggled -> {
                _state.update { it.copy(notificationsEnabled = intent.enabled) }
            }
            is SettingsIntent.DarkModeToggled -> {
                _state.update { it.copy(darkModeEnabled = intent.enabled) }
            }
            SettingsIntent.EditProfileClicked -> {
                // Handle navigation to edit profile
            }
            SettingsIntent.PrivacyPolicyClicked -> {
                // Handle navigation to privacy policy
            }
        }
    }

    private fun logout() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            logoutUseCase()
            _state.update { it.copy(isLoading = false) }
            _effect.emit(SettingsEffect.NavigateToLogin)
        }
    }
}
