package com.connectapp.presentation.profile

import com.connectapp.domain.model.User
import com.connectapp.presentation.util.UiText

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val showRecovery: Boolean = false,
    val recoveryPassword: String = "",
    val recoveryCode: String? = null,
    val isRecoveryLoading: Boolean = false,
    val showBackup: Boolean = false,
    val backupPassphrase: String = "",
    val backupEnvelope: String? = null,
    val restoreContent: String? = null,
    val showRestoreConfirmation: Boolean = false,
    val isBackupLoading: Boolean = false,
    val errorMessage: UiText? = null
)
