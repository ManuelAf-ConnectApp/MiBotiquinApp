package com.connectapp.presentation.navigation


import kotlinx.serialization.Serializable

sealed interface NavigationRoute {
    @Serializable
    data object SplashRoute : NavigationRoute

    @Serializable
    data object HomeRoute : NavigationRoute

    @Serializable
    data object LoginRoute : NavigationRoute

    @Serializable
    data object RegisterRoute : NavigationRoute

    @Serializable
    data object ForgotPasswordRoute : NavigationRoute

    data object SettingsRoute : NavigationRoute

    data object ProfileRoute : NavigationRoute

    val showBackButton: Boolean get() = this !is HomeRoute
    val showTopBar: Boolean get() = this !is SplashRoute && this !is LoginRoute
    val showBottomBar: Boolean
        get() = this !is SplashRoute && this !is LoginRoute && this !is RegisterRoute && this !is ForgotPasswordRoute
}