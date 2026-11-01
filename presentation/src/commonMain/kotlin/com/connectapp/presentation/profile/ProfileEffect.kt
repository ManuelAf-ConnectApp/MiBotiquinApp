package com.connectapp.presentation.profile

sealed interface ProfileEffect {
    data object NavigateToLogin : ProfileEffect
    data object NavigateBack : ProfileEffect
    data class SaveBackup(val content: String) : ProfileEffect
    data object OpenBackupFile : ProfileEffect
}
