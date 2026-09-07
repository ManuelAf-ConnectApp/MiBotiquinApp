package com.connectapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.*
import com.connectapp.domain.usecase.LoginUseCase
import com.connectapp.domain.validator.FormValidator
import com.connectapp.presentation.util.UiText
import com.connectapp.presentation.util.asUiText
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

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val formValidator: FormValidator
) : ViewModel() {

    private val _state: MutableStateFlow<LoginState> = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> get() = _state.asStateFlow()

    private val _intents: MutableSharedFlow<LoginIntent> = MutableSharedFlow()
    val intents: SharedFlow<LoginIntent> get() = _intents.asSharedFlow()

    private val _effect: MutableSharedFlow<LoginEffect> = MutableSharedFlow()
    val effect: SharedFlow<LoginEffect> get() = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }

            delay(2_000.milliseconds)

            _state.update {
                it.copy(
                    isLoading = false,
                )
            }
        }
    }

    fun onIntent(intent: LoginIntent) {
        reduce(intent)
    }

    private fun emitEffect(effect: LoginEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private fun reduce(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> {
                onEmailChanged(intent.email)
            }

            is LoginIntent.PasswordChanged -> {
                onPasswordChanged(intent.password)
            }

            LoginIntent.LoginClicked -> {
                login()
            }

            LoginIntent.RegisterClicked -> {
                emitEffect(LoginEffect.NavigateToRegister)
            }

            LoginIntent.TogglePasswordVisibility -> {
                onTogglePasswordVisibility()
            }

            LoginIntent.ToggleRememberMe -> {
                _state.update { it.copy(rememberMe = !it.rememberMe) }
            }

            LoginIntent.NavigateToForgotPassword -> {
                emitEffect(LoginEffect.NavigateToForgotPassword)
            }
        }
    }

    private fun onEmailChanged(email: String) {
        _state.update {
            it.copy(email = email)
        }
    }

    private fun onPasswordChanged(password: String) {
        _state.update {
            it.copy(password = password)
        }
    }

    private fun onTogglePasswordVisibility() {
        _state.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    private fun login() {
        val email = state.value.email
        if (!formValidator.isValidEmail(email)) {
            _state.update { it.copy(errorMessage = UiText.StringRes(error_invalid_email)) }
            return
        }

        val password = state.value.password
        if (!formValidator.isValidPassword(password)) {
            _state.update { it.copy(errorMessage = UiText.StringRes(error_invalid_password)) }
            return
        }


        _state.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        viewModelScope.launch {
            loginUseCase(
                email = state.value.email,
                password = state.value.password,
                rememberMe = state.value.rememberMe
            ).onSuccess { user ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            email = "",
                            password = ""
                        )
                    }
                    emitEffect(LoginEffect.NavigateToHome(user = user))
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