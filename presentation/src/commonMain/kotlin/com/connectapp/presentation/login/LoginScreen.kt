package com.connectapp.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import com.connectapp.presentation.component.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import com.connectapp.domain.model.User
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
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

    LoginScreenContent(modifier, state, onIntent)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LoginScreenContent(
    modifier: Modifier = Modifier, state: LoginState, onIntent: (LoginIntent) -> Unit
) {
    AuthLayout(modifier) {
        Column(Modifier.fillMaxWidth().padding(top = Dimen24, bottom = Dimen24), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(mibotiquin_icon),
                contentDescription = stringResource(app_name_full),
                modifier = Modifier.size(Dimen100)
            )
            Spacer(Modifier.height(Dimen16))
            Text(stringResource(main_title), style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(stringResource(brand_tagline), style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SectionIntro(stringResource(login_title), compact = true)
        AppTextField(state.email, { onIntent(LoginIntent.EmailChanged(it)) }, stringResource(label_email),
            keyboardType = KeyboardType.Email, trailingIcon = Icons.Default.Email,
            enabled = !state.isLoading, isError = state.errorMessage != null)
        PasswordField(state.password, { onIntent(LoginIntent.PasswordChanged(it)) }, stringResource(label_password),
            state.isPasswordVisible, { onIntent(LoginIntent.TogglePasswordVisibility) },
            enabled = !state.isLoading, isError = state.errorMessage != null)
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(Dimen4)) {
            Row(Modifier.heightIn(min = Dimen48).toggleable(state.rememberMe, enabled = !state.isLoading,
                role = Role.Checkbox, onValueChange = { onIntent(LoginIntent.ToggleRememberMe) }),
                verticalAlignment = Alignment.CenterVertically) {
                Checkbox(state.rememberMe, onCheckedChange = null, enabled = !state.isLoading)
                Spacer(Modifier.width(Dimen8))
                Text(stringResource(login_keep_session), style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { onIntent(LoginIntent.NavigateToForgotPassword) }, enabled = !state.isLoading) {
                Text(stringResource(login_forgot_password))
            }
        }
        state.errorMessage?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        PrimaryActionButton(stringResource(button_login), { onIntent(LoginIntent.LoginClicked) },
            enabled = state.isFormValid, busy = state.isLoading)
        Spacer(Modifier.height(Dimen16))
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
            itemVerticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(login_no_account), color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { onIntent(LoginIntent.RegisterClicked) }, enabled = !state.isLoading) {
                Text(stringResource(login_register_now), fontWeight = FontWeight.Bold)
            }
        }
    }
}
