package com.connectapp.presentation.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val viewModel: RegisterViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateToLogin -> onNavigateBack()
                RegisterEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(register_title), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = state.name,
            onValueChange = { viewModel.onIntent(RegisterIntent.NameChanged(it)) },
            label = { Text(stringResource(label_full_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.email,
            onValueChange = { viewModel.onIntent(RegisterIntent.EmailChanged(it)) },
            label = { Text(stringResource(label_email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.password,
            onValueChange = { viewModel.onIntent(RegisterIntent.PasswordChanged(it)) },
            label = { Text(stringResource(label_password)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.confirmPassword,
            onValueChange = { viewModel.onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
            label = { Text(stringResource(label_confirm_password)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.onIntent(RegisterIntent.RegisterClicked) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(button_register))
            }
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = { viewModel.onIntent(RegisterIntent.LoginClicked) }) {
            Text(stringResource(register_already_account))
        }
    }
}
