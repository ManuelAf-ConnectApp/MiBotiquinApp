package com.connectapp.presentation.login

import com.connectapp.presentation.util.UiText

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errorMessage: UiText? = null
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
}
