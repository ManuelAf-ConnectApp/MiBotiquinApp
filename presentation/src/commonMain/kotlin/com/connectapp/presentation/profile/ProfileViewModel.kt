package com.connectapp.presentation.profile

import com.connectapp.domain.usecase.GetCurrentUserUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.DeleteAccountUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        loadProfile()
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
                            errorMessage = exception.message
                        )
                    }
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            logoutUseCase().onSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(ProfileEffect.NavigateToLogin)
            }
        }
    }

    private fun deleteAccount() {
        val email = _state.value.user?.email ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            deleteAccountUseCase(email).onSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(ProfileEffect.NavigateToLogin)
            }
        }
    }
}
