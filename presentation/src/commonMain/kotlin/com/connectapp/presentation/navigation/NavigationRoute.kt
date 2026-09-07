package com.connectapp.presentation.navigation


import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User
import kotlinx.serialization.Serializable

sealed interface NavigationRoute {
    @Serializable
    data object SplashRoute : NavigationRoute

    @Serializable
    data class HomeRoute(val user: User? = null) : NavigationRoute

    @Serializable
    data object LoginRoute : NavigationRoute

    @Serializable
    data object RegisterRoute : NavigationRoute

    @Serializable
    data object ForgotPasswordRoute : NavigationRoute

    data object SettingsRoute : NavigationRoute

    @Serializable
    data class ProfileRoute(val user: User? = null) : NavigationRoute
    
    @Serializable
    data class MedicationRoute(
        val user: User? = null,
        val medication: Medication? = null
    ) : NavigationRoute

    val showBackButton: Boolean get() = this !is HomeRoute
    val showTopBar: Boolean get() = this !is SplashRoute && this !is LoginRoute
    val showBottomBar: Boolean
        get() = this !is SplashRoute && this !is LoginRoute && this !is RegisterRoute && this !is ForgotPasswordRoute && this !is MedicationRoute
}