package com.connectapp.presentation.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.connectapp.presentation.forgot_password.ForgotPasswordScreenContent
import com.connectapp.presentation.forgot_password.ForgotPasswordState
import com.connectapp.presentation.login.LoginScreenContent
import com.connectapp.presentation.login.LoginState
import com.connectapp.presentation.register.RegisterScreenContent
import com.connectapp.presentation.register.RegisterState
import com.connectapp.presentation.settings.SettingsScreenContent
import com.connectapp.presentation.settings.SettingsState
import com.connectapp.presentation.theme.*

@Composable
private fun PreviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) AppDarkColorScheme else AppLightColorScheme,
        typography = AppTypography, shapes = AppShapes
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
    }
}

@Preview(name = "Login · light", widthDp = 390, heightDp = 844, locale = "es")
@Preview(name = "Login · dark", widthDp = 390, heightDp = 844, locale = "es", uiMode = 0x20)
@Composable
fun LoginDesignPreview() = PreviewTheme { LoginScreenContent(state = LoginState(), onIntent = {}) }

@Preview(name = "Register · light", widthDp = 430, heightDp = 932, locale = "es")
@Preview(name = "Register · compact, large text", widthDp = 320, heightDp = 640, locale = "es", fontScale = 1.5f)
@Composable
fun RegisterDesignPreview() = PreviewTheme { RegisterScreenContent(state = RegisterState(), onIntent = {}) }

@Preview(name = "Local recovery", widthDp = 390, heightDp = 844, locale = "es")
@Composable
fun RecoveryDesignPreview() = PreviewTheme { ForgotPasswordScreenContent(state = ForgotPasswordState(), onIntent = {}) }

@Preview(name = "Settings · light", widthDp = 390, heightDp = 844, locale = "es")
@Preview(name = "Settings · dark", widthDp = 390, heightDp = 844, locale = "es", uiMode = 0x20)
@Composable
fun SettingsDesignPreview() = PreviewTheme {
    SettingsScreenContent(state = SettingsState(), onProfile = {}, onSecurity = {}, onNotifications = {},
        onDarkMode = {}, onAbout = {}, onPrivacy = {}, onLogout = {})
}
