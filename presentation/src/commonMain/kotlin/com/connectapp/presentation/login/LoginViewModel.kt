package com.connectapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.LoginUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
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

            delay(2_000)

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
                _state.update {
                    it.copy(email = intent.email)
                }
            }

            is LoginIntent.PasswordChanged -> {
                _state.update {
                    it.copy(password = intent.password)
                }
            }

            LoginIntent.LoginClicked -> {
                login()
            }

            LoginIntent.RegisterClicked -> {
                emitEffect(LoginEffect.NavigateToRegister)
            }

            LoginIntent.TogglePasswordVisibility -> {
                _state.update {
                    it.copy(isPasswordVisible = !it.isPasswordVisible)
                }
            }
        }
    }

    private fun login() {
        _state.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            loginUseCase(state.value.email, state.value.password)
                .onSuccess {
                    _state.update {
                        it.copy(isLoading = false, isLoggedIn = true)
                    }
                    emitEffect(LoginEffect.NavigateToHome)
                }.onFailure {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = it.errorMessage)
                    }
                }
        }
    }

}