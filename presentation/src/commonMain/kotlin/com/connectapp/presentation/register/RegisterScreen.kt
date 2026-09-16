package com.connectapp.presentation.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToHome: (com.connectapp.domain.model.User) -> Unit
) {
    val viewModel: RegisterViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateToLogin -> onNavigateBack()
                is RegisterEffect.NavigateToHome -> onNavigateToHome(effect.user)
            }
        }
    }

    RegisterScreenContent(
        modifier = modifier.fillMaxSize(),
        state = state,
        onIntent = onIntent
    )


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreenContent(
    modifier: Modifier = Modifier,
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit
) {

    Column(
        modifier = modifier
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimen24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimen48))

            Text(
                text = stringResource(register_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Dimen32))

            OutlinedTextField(
                value = state.name,
                onValueChange = { onIntent(RegisterIntent.NameChanged(it)) },
                label = { Text(stringResource(label_full_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(Dimen16))

            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
                label = { Text(stringResource(label_email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(Dimen16))

            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                label = { Text(stringResource(label_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image =
                        if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = {
                        onIntent(RegisterIntent.TogglePasswordVisibility)
                    }) {
                        Icon(
                            imageVector = image,
                            contentDescription = stringResource(content_desc_toggle_password)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(Dimen16))

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
                label = { Text(stringResource(label_confirm_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image =
                        if (state.isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = {
                        onIntent(RegisterIntent.ToggleConfirmPasswordVisibility)
                    }) {
                        Icon(
                            imageVector = image,
                            contentDescription = stringResource(content_desc_toggle_password)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(Dimen32))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { onIntent(RegisterIntent.RegisterClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimen50),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        stringResource(button_register),
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
            
            Spacer(modifier = Modifier.height(Dimen16))
        }

        TextButton(
            onClick = { onIntent(RegisterIntent.LoginClicked) },
            modifier = Modifier.padding(bottom = Dimen16)
        ) {
            Text(stringResource(register_already_account))
        }
    }
}
