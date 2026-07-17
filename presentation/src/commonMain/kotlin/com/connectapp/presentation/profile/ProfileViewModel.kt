package com.connectapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            // Simulate loading user settings
            delay(1_000)

            _state.update {
                it.copy(
                    isLoading = false,
                )
            }
            loadProfile()
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.BackClicked -> {
                viewModelScope.launch {
                    _effect.emit(ProfileEffect.NavigateBack)
                }
            }

            ProfileIntent.LogoutClicked -> {
                logout()
            }

            ProfileIntent.LoadProfile -> {
                loadProfile()
            }
        }
    }

    private fun loadProfile() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // Simulate API call
            delay(1500.milliseconds)
            val dummyUser = User(
                id = "1",
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                phone = "+123456789"
            )
            _state.update {
                it.copy(
                    isLoading = false,
                    user = dummyUser
                )
            }
        }
    }

    private fun logout() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // Simulate logout
            delay(1000.milliseconds)
            _state.update { it.copy(isLoading = false) }
            _effect.emit(ProfileEffect.NavigateToLogin)
        }
    }
}
