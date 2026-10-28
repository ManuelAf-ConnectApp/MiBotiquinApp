package com.connectapp.presentation.profile

import com.connectapp.domain.usecase.GetCurrentUserUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import com.connectapp.domain.usecase.DeleteAccountUseCase
import com.connectapp.domain.usecase.IssueRecoveryCodeUseCase
import com.connectapp.domain.usecase.ExportBackupUseCase
import com.connectapp.domain.usecase.RestoreBackupUseCase
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
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val issueRecoveryCode: IssueRecoveryCodeUseCase,
    private val exportBackup: ExportBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase
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
            ProfileIntent.OpenRecovery -> _state.update { it.copy(showRecovery = true, errorMessage = null) }
            is ProfileIntent.RecoveryPasswordChanged -> _state.update { it.copy(recoveryPassword = intent.password, errorMessage = null) }
            ProfileIntent.GenerateRecovery -> generateRecovery()
            ProfileIntent.CloseRecovery -> if (!_state.value.isRecoveryLoading) _state.update {
                it.copy(showRecovery = false, recoveryPassword = "", recoveryCode = null, errorMessage = null)
            }
            ProfileIntent.OpenBackup -> _state.update { it.copy(showBackup = true, errorMessage = null) }
            is ProfileIntent.BackupPassphraseChanged -> _state.update { it.copy(backupPassphrase = intent.value, errorMessage = null) }
            ProfileIntent.CloseBackup -> _state.update { it.copy(showBackup = false, backupPassphrase = "", backupEnvelope = null, errorMessage = null) }
            ProfileIntent.ExportBackup -> exportBackup()
            ProfileIntent.OpenRestore -> viewModelScope.launch { _effect.emit(ProfileEffect.OpenBackupFile) }
            is ProfileIntent.BackupFileSelected -> _state.update { it.copy(restoreContent = intent.content, showRestoreConfirmation = true, errorMessage = null) }
            ProfileIntent.RestoreBackup -> restoreBackup()
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

    private fun restoreBackup() {
        val content = _state.value.restoreContent ?: return
        val passphrase = _state.value.backupPassphrase
        if (passphrase.length < 12 || _state.value.isBackupLoading) return
        _state.update { it.copy(isBackupLoading = true, errorMessage = null) }
        viewModelScope.launch {
            restoreBackup(content, passphrase.toCharArray()).onSuccess {
                _state.update { it.copy(isBackupLoading = false, showRestoreConfirmation = false, restoreContent = null, backupPassphrase = "") }
            }.onFailure { error -> _state.update { it.copy(isBackupLoading = false, errorMessage = error.asUiText()) } }
        }
    }

    private fun exportBackup() {
        val passphrase = _state.value.backupPassphrase
        if (passphrase.length < 12 || _state.value.isBackupLoading) return
        _state.update { it.copy(isBackupLoading = true, errorMessage = null) }
        viewModelScope.launch {
            exportBackup(passphrase.toCharArray()).onSuccess { content ->
                _state.update { it.copy(isBackupLoading = false, backupPassphrase = "", backupEnvelope = content) }
                _effect.emit(ProfileEffect.SaveBackup(content))
            }.onFailure { error -> _state.update { it.copy(isBackupLoading = false, errorMessage = error.asUiText()) } }
        }
    }

    private fun generateRecovery() {
        val password = _state.value.recoveryPassword
        if (password.isBlank() || _state.value.isRecoveryLoading || _state.value.recoveryCode != null) return
        _state.update { it.copy(isRecoveryLoading = true, errorMessage = null) }
        viewModelScope.launch {
            issueRecoveryCode(password).onSuccess { code ->
                _state.update { it.copy(isRecoveryLoading = false, recoveryPassword = "", recoveryCode = code) }
            }.onFailure { error ->
                _state.update { it.copy(isRecoveryLoading = false, errorMessage = error.asUiText()) }
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
            deleteAccountUseCase().onSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(ProfileEffect.NavigateToLogin)
            }
        }
    }
}
