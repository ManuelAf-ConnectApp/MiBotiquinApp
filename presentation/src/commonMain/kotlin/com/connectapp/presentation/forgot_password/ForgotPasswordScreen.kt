package com.connectapp.presentation.forgot_password

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    val viewModel: ForgotPasswordViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ForgotPasswordEffect.NavigateToLogin -> onNavigateBack()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(forgot_password_title), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(forgot_password_instruction),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = state.email,
            onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
            label = { Text(stringResource(label_email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.isEmailSent) {
            Text(
                text = stringResource(forgot_password_success),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(
                onClick = { onIntent(ForgotPasswordIntent.SendEmailClicked) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(button_send_reset_link))
            }
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = { onIntent(ForgotPasswordIntent.BackToLoginClicked) }) {
            Text(stringResource(button_back_to_login))
        }
    }
}
