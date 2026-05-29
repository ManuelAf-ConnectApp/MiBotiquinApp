package com.connectapp.presentation.register

sealed interface RegisterIntent {
    data class NameChanged(val name: String) : RegisterIntent
    data class EmailChanged(val email: String) : RegisterIntent
    data class PasswordChanged(val password: String) : RegisterIntent
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterIntent
    object RegisterClicked : RegisterIntent
    object LoginClicked : RegisterIntent
}
