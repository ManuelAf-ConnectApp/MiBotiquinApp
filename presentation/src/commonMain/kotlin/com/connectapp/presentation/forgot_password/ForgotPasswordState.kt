package com.connectapp.presentation.forgot_password

import com.connectapp.presentation.util.UiText

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val errorMessage: UiText? = null
)
