package com.connectapp.presentation.splash

sealed interface SplashEffect {
    object NavigateToLogin : SplashEffect
    data class NavigateToHome(val user: com.connectapp.domain.model.User) : SplashEffect
}
