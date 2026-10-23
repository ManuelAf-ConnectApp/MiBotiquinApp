package com.connectapp.presentation.forgot_password

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimen24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Dimen64))

        Text(
            text = stringResource(forgot_password_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimen16))

        Text(
            text = stringResource(forgot_password_instruction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dimen32))

        OutlinedTextField(
            value = state.email,
            onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
            label = { Text(stringResource(label_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(Dimen32))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.isEmailSent) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(forgot_password_success),
                    modifier = Modifier.padding(Dimen16),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Button(
                onClick = { onIntent(ForgotPasswordIntent.SendEmailClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen50),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    stringResource(button_send_reset_link),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        state.errorMessage?.let {
            Spacer(modifier = Modifier.height(Dimen16))
            Text(
                text = it.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = { onIntent(ForgotPasswordIntent.BackToLoginClicked) },
            modifier = Modifier.padding(bottom = Dimen16)
        ) {
            Text(stringResource(button_back_to_login))
        }
    }
}
