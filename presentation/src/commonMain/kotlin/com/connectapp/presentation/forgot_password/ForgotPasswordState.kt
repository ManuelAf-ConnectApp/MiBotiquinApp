package com.connectapp.presentation.forgot_password

import com.connectapp.presentation.util.UiText

data class ForgotPasswordState(
    val email: String = "",
    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isReset: Boolean = false,
    val errorMessage: UiText? = null
)
