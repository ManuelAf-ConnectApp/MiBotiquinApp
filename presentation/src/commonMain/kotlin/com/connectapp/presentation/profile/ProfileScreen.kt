package com.connectapp.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
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

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateBack -> onNavigateBack()
                ProfileEffect.NavigateToLogin -> onNavigateToLogin()
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
                onDeleteAccount = { showDeleteConfirmation = true }
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
    onDeleteAccount: () -> Unit
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
