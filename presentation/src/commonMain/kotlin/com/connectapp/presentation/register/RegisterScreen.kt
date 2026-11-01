package com.connectapp.presentation.register

import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalDensity
import com.connectapp.presentation.component.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
internal fun RegisterScreenContent(
    modifier: Modifier = Modifier, state: RegisterState, onIntent: (RegisterIntent) -> Unit
) {
    AuthLayout(modifier) {
        SectionIntro(stringResource(register_title), stringResource(register_subtitle))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val density = LocalDensity.current
            if (maxWidth >= Dimen280 && density.fontScale <= 1.2f) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimen16)) {
                    AppTextField(state.name, { onIntent(RegisterIntent.NameChanged(it)) }, stringResource(label_name),
                        modifier = Modifier.weight(1f), enabled = !state.isLoading)
                    AppTextField(state.lastName, { onIntent(RegisterIntent.LastNameChanged(it)) }, stringResource(label_last_name),
                        modifier = Modifier.weight(1f), enabled = !state.isLoading)
                }
            } else Column(verticalArrangement = Arrangement.spacedBy(Dimen16)) {
                AppTextField(state.name, { onIntent(RegisterIntent.NameChanged(it)) }, stringResource(label_name), enabled = !state.isLoading)
                AppTextField(state.lastName, { onIntent(RegisterIntent.LastNameChanged(it)) }, stringResource(label_last_name), enabled = !state.isLoading)
            }
        }
        AppTextField(state.email, { onIntent(RegisterIntent.EmailChanged(it)) }, stringResource(label_email),
            keyboardType = KeyboardType.Email, enabled = !state.isLoading)
        PasswordField(state.password, { onIntent(RegisterIntent.PasswordChanged(it)) }, stringResource(label_password),
            state.isPasswordVisible, { onIntent(RegisterIntent.TogglePasswordVisibility) }, enabled = !state.isLoading)
        PasswordField(state.confirmPassword, { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) }, stringResource(label_confirm_password),
            state.isConfirmPasswordVisible, { onIntent(RegisterIntent.ToggleConfirmPasswordVisibility) }, enabled = !state.isLoading)
        state.errorMessage?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        PrimaryActionButton(stringResource(button_register), { onIntent(RegisterIntent.RegisterClicked) }, busy = state.isLoading)
        TextButton(onClick = { onIntent(RegisterIntent.LoginClicked) }, modifier = Modifier.align(Alignment.CenterHorizontally), enabled = !state.isLoading) {
            Text(stringResource(register_already_account))
        }
    }
}
