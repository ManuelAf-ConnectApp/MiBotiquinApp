package com.connectapp.presentation.register

sealed interface RegisterEffect {
    object NavigateToLogin : RegisterEffect
    data class NavigateToHome(val user: com.connectapp.domain.model.User) : RegisterEffect
}
