package com.connectapp.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.commonresources.resources.Res
import com.connectapp.commonresources.resources.backup_create
import com.connectapp.commonresources.resources.backup_export_warning
import com.connectapp.commonresources.resources.backup_passphrase
import com.connectapp.commonresources.resources.backup_restore
import com.connectapp.commonresources.resources.backup_title
import com.connectapp.domain.model.User
import com.connectapp.presentation.profile.component.ProfileInfoItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    user: User?,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    // El usuario de la sesión activa (recibido por navegación) siempre tiene prioridad
    // sobre el que intenta recuperar el ViewModel de almacenamiento local, que solo
    // está disponible si el usuario marcó "recordarme" al iniciar sesión.
    val resolvedState = state.copy(user = user ?: state.user)
    var contentToSave by remember { mutableStateOf<String?>(null) }
    var openRequested by remember { mutableStateOf(false) }

    BackupFilePicker(
        contentToSave = contentToSave,
        openRequested = openRequested,
        onSaveHandled = { contentToSave = null; onIntent(ProfileIntent.CloseBackup) },
        onFileOpened = { content ->
            openRequested = false
            if (content != null) onIntent(ProfileIntent.BackupFileSelected(content)) else onIntent(ProfileIntent.CloseBackup)
        }
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateBack -> onNavigateBack()
                ProfileEffect.NavigateToLogin -> onNavigateToLogin()
                is ProfileEffect.SaveBackup -> contentToSave = effect.content
                ProfileEffect.OpenBackupFile -> openRequested = true
            }
        }
    }
    if (resolvedState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        var showDeleteConfirmation by remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ProfileScreenContent(
                state = resolvedState,
                onLogout = { onIntent(ProfileIntent.LogoutClicked) },
                onDeleteAccount = { showDeleteConfirmation = true },
                onRecovery = { onIntent(ProfileIntent.OpenRecovery) },
                onBackup = { onIntent(ProfileIntent.OpenBackup) }
            )
        }

        if (state.showRecovery) {
            AlertDialog(
                onDismissRequest = { if (state.recoveryCode == null) onIntent(ProfileIntent.CloseRecovery) },
                title = { Text(stringResource(recovery_code_title)) },
                text = {
                    Column {
                        if (state.recoveryCode != null) {
                            Text(stringResource(recovery_code_saved_warning))
                            Spacer(Modifier.height(Dimen16))
                            Text(state.recoveryCode!!, style = MaterialTheme.typography.titleLarge)
                        } else {
                            Text(stringResource(recovery_code_instruction))
                            Spacer(Modifier.height(Dimen16))
                            OutlinedTextField(value = state.recoveryPassword,
                                onValueChange = { onIntent(ProfileIntent.RecoveryPasswordChanged(it)) },
                                label = { Text(stringResource(label_password)) }, singleLine = true,
                                enabled = !state.isRecoveryLoading,
                                visualTransformation = PasswordVisualTransformation())
                        }
                        if (state.isRecoveryLoading) CircularProgressIndicator()
                        state.errorMessage?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = { TextButton(
                    enabled = !state.isRecoveryLoading && (state.recoveryCode != null || state.recoveryPassword.isNotBlank()),
                    onClick = { onIntent(if (state.recoveryCode != null) ProfileIntent.CloseRecovery else ProfileIntent.GenerateRecovery) }
                ) { Text(stringResource(if (state.recoveryCode != null) recovery_saved else recovery_generate)) } },
                dismissButton = { if (state.recoveryCode == null) TextButton(onClick = { onIntent(ProfileIntent.CloseRecovery) }) { Text(stringResource(button_cancel)) } }
            )
        }

        if (state.showBackup) {
            AlertDialog(
                onDismissRequest = { if (!state.isBackupLoading) onIntent(ProfileIntent.CloseBackup) },
                title = { Text(stringResource(Res.string.backup_title)) },
                text = {
                    Column {
                        Text(stringResource(Res.string.backup_export_warning))
                        Spacer(Modifier.height(Dimen16))
                        OutlinedTextField(
                            value = state.backupPassphrase,
                            onValueChange = { onIntent(ProfileIntent.BackupPassphraseChanged(it)) },
                            label = { Text(stringResource(Res.string.backup_passphrase)) }, singleLine = true,
                            enabled = !state.isBackupLoading,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        if (state.isBackupLoading) CircularProgressIndicator()
                        state.errorMessage?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = { TextButton(
                    enabled = !state.isBackupLoading && state.backupPassphrase.length >= 12,
                    onClick = { onIntent(ProfileIntent.ExportBackup) }
                ) { Text(stringResource(Res.string.backup_create)) } },
                dismissButton = { Row {
                    TextButton(onClick = { onIntent(ProfileIntent.OpenRestore) }) { Text(stringResource(Res.string.backup_restore)) }
                    TextButton(onClick = { onIntent(ProfileIntent.CloseBackup) }) { Text(stringResource(button_cancel)) }
                } }
            )
        }

        if (state.showRestoreConfirmation) {
            AlertDialog(
                onDismissRequest = { if (!state.isBackupLoading) onIntent(ProfileIntent.CloseBackup) },
                title = { Text(stringResource(Res.string.backup_restore)) },
                text = { Text(stringResource(Res.string.backup_export_warning)) },
                confirmButton = { TextButton(
                    enabled = !state.isBackupLoading && state.backupPassphrase.length >= 12,
                    onClick = { onIntent(ProfileIntent.RestoreBackup) }
                ) { Text(stringResource(Res.string.backup_restore)) } },
                dismissButton = { TextButton(onClick = { onIntent(ProfileIntent.CloseBackup) }) { Text(stringResource(button_cancel)) } }
            )
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text(stringResource(delete_account_confirmation_title)) },
                text = { Text(stringResource(delete_account_confirmation_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            onIntent(ProfileIntent.DeleteAccountClicked)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(button_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text(stringResource(button_cancel))
                    }
                }
            )
        }
    }

}

@Composable
private fun ProfileScreenContent(
    state: ProfileState,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onRecovery: () -> Unit,
    onBackup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimen24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.user?.let { user ->
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(Dimen100),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Dimen24))

            ProfileInfoItem(
                icon = Icons.Default.Person,
                label = stringResource(label_name),
                value = "${user.firstName} ${user.lastName}".trim()
            )

            Spacer(modifier = Modifier.height(Dimen16))

            ProfileInfoItem(
                icon = Icons.Default.Email,
                label = stringResource(label_email),
                value = user.email
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = onRecovery, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Key, contentDescription = null)
                Spacer(Modifier.width(Dimen8))
                Text(stringResource(recovery_code_title))
            }

            Spacer(modifier = Modifier.height(Dimen8))

            OutlinedButton(onClick = onBackup, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.backup_title))
            }

            Spacer(modifier = Modifier.height(Dimen8))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(stringResource(logout))
            }

            Spacer(modifier = Modifier.height(Dimen8))

            TextButton(
                onClick = onDeleteAccount,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(delete_account))
            }
        } ?: Text(stringResource(error_user_not_found))
    }
}
