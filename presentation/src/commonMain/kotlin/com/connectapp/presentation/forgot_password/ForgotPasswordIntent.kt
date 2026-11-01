package com.connectapp.presentation.forgot_password

sealed interface ForgotPasswordIntent {
    data class EmailChanged(val email: String) : ForgotPasswordIntent
    data class CodeChanged(val code: String) : ForgotPasswordIntent
    data class PasswordChanged(val password: String) : ForgotPasswordIntent
    data class ConfirmPasswordChanged(val password: String) : ForgotPasswordIntent
    object ResetClicked : ForgotPasswordIntent
    object BackToLoginClicked : ForgotPasswordIntent
}
