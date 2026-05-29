package com.connectapp.presentation.navigation

data class NavigationState(
    val route: NavigationRoute,
)

data class SnackBarState(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

