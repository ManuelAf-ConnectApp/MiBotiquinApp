package com.connectapp.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.domain.model.User
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome: (User) -> Unit,
) {
    val viewModel: LoginViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onNavigateToHome(effect.user)
                is LoginEffect.NavigateToRegister -> onNavigateToRegister()
                is LoginEffect.NavigateToForgotPassword -> onNavigateToForgotPassword()
                is LoginEffect.ShowError -> {

                }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoginScreenContent(
                modifier = modifier,
                state = state,
                onIntent = onIntent,
            )
        }
    }

}

@Composable
private fun LoginScreenContent(
    modifier: Modifier = Modifier,
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = Dimen24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(login_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Dimen8))

            Text(
                text = stringResource(login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(Dimen32))

            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
                label = { Text(stringResource(label_email)) },
                placeholder = { Text(stringResource(placeholder_email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.errorMessage != null
            )

            Spacer(modifier = Modifier.height(Dimen16))

            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                label = { Text(stringResource(label_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.errorMessage != null,
                trailingIcon = {
                    val image =
                        if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = {
                        onIntent(LoginIntent.TogglePasswordVisibility)
                    }) {
                        Icon(
                            imageVector = image,
                            contentDescription = stringResource(content_desc_toggle_password)
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = state.rememberMe,
                    onCheckedChange = { onIntent(LoginIntent.ToggleRememberMe) }
                )
                Text(
                    text = stringResource(login_keep_session),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = {
                    onIntent(LoginIntent.NavigateToForgotPassword)
                }) {
                    Text(stringResource(login_forgot_password))
                }
            }


            Spacer(modifier = Modifier.height(Dimen24))

            Button(
                onClick = { onIntent(LoginIntent.LoginClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen50),
                shape = MaterialTheme.shapes.medium,
                enabled = state.isFormValid
            ) {
                Text(stringResource(button_login), style = MaterialTheme.typography.titleMedium)
            }

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(Dimen16))
                Text(
                    text = it.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(login_no_account))
            TextButton(onClick = {
                onIntent(LoginIntent.RegisterClicked)
            }) {
                Text(stringResource(login_register_now))
            }
        }

        Spacer(modifier = Modifier.height(Dimen16))
    }
}
