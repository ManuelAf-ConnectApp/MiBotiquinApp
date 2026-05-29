package com.connectapp.presentation.login

sealed interface LoginEffect {
    data class ShowError(val message: String) : LoginEffect
    object NavigateToHome : LoginEffect
    object NavigateToRegister : LoginEffect
    object NavigateToForgotPassword : LoginEffect
}