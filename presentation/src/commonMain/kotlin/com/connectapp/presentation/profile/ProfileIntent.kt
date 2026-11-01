package com.connectapp.presentation.profile

sealed interface ProfileIntent {
    data object OpenRecovery : ProfileIntent
    data class RecoveryPasswordChanged(val password: String) : ProfileIntent
    data object GenerateRecovery : ProfileIntent
    data object CloseRecovery : ProfileIntent
    data object OpenBackup : ProfileIntent
    data class BackupPassphraseChanged(val value: String) : ProfileIntent
    data object ExportBackup : ProfileIntent
    data object OpenRestore : ProfileIntent
    data class BackupFileSelected(val content: String) : ProfileIntent
    data object RestoreBackup : ProfileIntent
    data object CloseBackup : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data object DeleteAccountClicked : ProfileIntent
    data object BackClicked : ProfileIntent
    data object LoadProfile : ProfileIntent
}
