package com.connectapp.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.*
import com.connectapp.domain.usecase.RegisterUseCase
import com.connectapp.domain.validator.FormValidator
import com.connectapp.presentation.util.UiText
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val formValidator: FormValidator
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect: SharedFlow<RegisterEffect> = _effect.asSharedFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.NameChanged -> onNameChanged(intent.name)
            is RegisterIntent.EmailChanged -> onEmailChanged(intent.email)
            is RegisterIntent.PasswordChanged -> onPasswordChanged(intent.password)
            is RegisterIntent.ConfirmPasswordChanged -> onConfirmPasswordChanged(intent.confirmPassword)
            RegisterIntent.RegisterClicked -> register()
            RegisterIntent.LoginClicked -> emitEffect(RegisterEffect.NavigateToLogin)
        }
    }

    private fun onNameChanged(name: String) {
        _state.update { it.copy(name = name) }
    }

    private fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    private fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    private fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword) }
    }

    private fun register() {
        val name = _state.value.name
        val email = _state.value.email
        val password = _state.value.password
        val confirmPassword = _state.value.confirmPassword

        if (name.isBlank()) {
            _state.update { it.copy(errorMessage = UiText.StringRes(error_empty_name)) }
            return
        }

        if (!formValidator.isValidEmail(email)) {
            _state.update { it.copy(errorMessage = UiText.StringRes(error_invalid_email)) }
            return
        }

        if (password.length < 6) {
            _state.update { it.copy(errorMessage = UiText.StringRes(error_password_too_short)) }
            return
        }

        if (password != confirmPassword) {
            _state.update { it.copy(errorMessage = UiText.StringRes(error_passwords_not_match)) }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            registerUseCase(name, email, password)
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false, isRegistered = true) }
                    emitEffect(RegisterEffect.NavigateToHome(user))
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.asUiText()) }
                }
        }
    }

    private fun emitEffect(effect: RegisterEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
