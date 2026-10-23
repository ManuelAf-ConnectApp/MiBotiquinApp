package com.connectapp.presentation.profile

import com.connectapp.domain.usecase.GetCurrentUserUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.DeleteAccountUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.presentation.util.asUiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProfileEffect>()
    val effect: SharedFlow<ProfileEffect> = _effect.asSharedFlow()

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

            ProfileIntent.DeleteAccountClicked -> {
                deleteAccount()
            }

            ProfileIntent.LoadProfile -> {
                loadProfile()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getCurrentUserUseCase()
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            user = user
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.asUiText()
                        )
                    }
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            logoutUseCase().onSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(ProfileEffect.NavigateToLogin)
            }.onFailure { exception ->
                _state.update { it.copy(isLoading = false, errorMessage = exception.asUiText()) }
            }
        }
    }

    // Borra la cuenta de la sesión activa: el repositorio la resuelve por sí mismo, así un
    // estado obsoleto de este ViewModel (compartido entre sesiones) no puede borrar otra cuenta.
    private fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            deleteAccountUseCase().onSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(ProfileEffect.NavigateToLogin)
            }.onFailure { exception ->
                _state.update { it.copy(isLoading = false, errorMessage = exception.asUiText()) }
            }
        }
    }
}
