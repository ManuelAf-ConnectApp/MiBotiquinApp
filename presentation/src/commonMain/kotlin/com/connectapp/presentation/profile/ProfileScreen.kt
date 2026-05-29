package com.connectapp.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.presentation.profile.component.ProfileInfoItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateBack -> onNavigateBack()
                ProfileEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }
    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ProfileScreenContent(
                state = state,
                onLogout = { onIntent(ProfileIntent.LogoutClicked) }
            )
        }
    }

}

@Composable
private fun ProfileScreenContent(
    state: ProfileState,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.user?.let { user ->
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileInfoItem(
                icon = Icons.Default.Person,
                label = stringResource(label_name),
                value = "${user.firstName} ${user.lastName}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoItem(
                icon = Icons.Default.Email,
                label = stringResource(label_email),
                value = user.email
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoItem(
                icon = Icons.Default.Phone,
                label = stringResource(label_phone),
                value = user.phone
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(logout))
            }
        } ?: Text(stringResource(error_user_not_found))
    }
}
