package com.connectapp.presentation.login

import com.connectapp.domain.model.User

sealed interface LoginEffect {
    data class ShowError(val message: String) : LoginEffect
    data class NavigateToHome(val user: User) : LoginEffect
    object NavigateToRegister : LoginEffect
    object NavigateToForgotPassword : LoginEffect
}