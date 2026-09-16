package com.connectapp.presentation.register

import com.connectapp.presentation.util.UiText

data class RegisterState(
    val name: String = "",
    val nameError: UiText? = null,
    val email: String = "",
    val emailError: UiText? = null,
    val password: String = "",
    val passwordError: UiText? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: UiText? = null,
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val errorMessage: UiText? = null
)
