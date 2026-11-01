package com.connectapp.presentation.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.*
import com.connectapp.domain.usecase.ResetPasswordUseCase
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

class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ForgotPasswordEffect>()
    val effect: SharedFlow<ForgotPasswordEffect> = _effect.asSharedFlow()

    fun onIntent(intent: ForgotPasswordIntent) {
        when (intent) {
            is ForgotPasswordIntent.EmailChanged -> _state.update { it.copy(email = intent.email, errorMessage = null) }
            is ForgotPasswordIntent.CodeChanged -> _state.update { it.copy(code = intent.code, errorMessage = null) }
            is ForgotPasswordIntent.PasswordChanged -> _state.update { it.copy(password = intent.password, errorMessage = null) }
            is ForgotPasswordIntent.ConfirmPasswordChanged -> _state.update { it.copy(confirmPassword = intent.password, errorMessage = null) }
            ForgotPasswordIntent.ResetClicked -> resetPassword()
            ForgotPasswordIntent.BackToLoginClicked -> emitEffect(ForgotPasswordEffect.NavigateToLogin)
        }
    }

    private fun resetPassword() {
        val form = _state.value
        val error = when {
            form.email.isBlank() || form.code.isBlank() || form.password.isBlank() || form.confirmPassword.isBlank() -> error_mandatory_fields
            form.password.length < 8 -> error_password_too_short
            form.password != form.confirmPassword -> error_passwords_not_match
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(errorMessage = UiText.StringRes(error)) }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            resetPasswordUseCase(form.email, form.code, form.password)
                .onSuccess {
                    _state.value = ForgotPasswordState(isReset = true)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.asUiText()) }
                }
        }
    }

    private fun emitEffect(effect: ForgotPasswordEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
