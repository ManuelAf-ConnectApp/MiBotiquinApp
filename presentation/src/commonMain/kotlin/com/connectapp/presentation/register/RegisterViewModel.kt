package com.connectapp.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect: SharedFlow<RegisterEffect> = _effect.asSharedFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.NameChanged -> _state.update { it.copy(name = intent.name) }
            is RegisterIntent.EmailChanged -> _state.update { it.copy(email = intent.email) }
            is RegisterIntent.PasswordChanged -> _state.update { it.copy(password = intent.password) }
            is RegisterIntent.ConfirmPasswordChanged -> _state.update { it.copy(confirmPassword = intent.confirmPassword) }
            RegisterIntent.RegisterClicked -> register()
            RegisterIntent.LoginClicked -> emitEffect(RegisterEffect.NavigateToLogin)
        }
    }

    private fun register() {
        if (_state.value.password != _state.value.confirmPassword) {
            _state.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            registerUseCase(_state.value.name, _state.value.email, _state.value.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isRegistered = true) }
                    emitEffect(RegisterEffect.NavigateToHome)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun emitEffect(effect: RegisterEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
