package com.connectapp.presentation

import androidx.lifecycle.ViewModel
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.navigation.NavigationState
import com.connectapp.presentation.navigation.SnackBarState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _navState = MutableStateFlow(NavigationState(route = NavigationRoute.SplashRoute))
    val navState: StateFlow<NavigationState> get() = _navState

    private val _snackBarState = MutableStateFlow<SnackBarState?>(null)
    val snackBarState: StateFlow<SnackBarState?> get() = _snackBarState

    private var currentUser: com.connectapp.domain.model.User? = null

    fun updateNavigationState(navRoute: NavigationRoute) {
        when (navRoute) {
            is NavigationRoute.HomeRoute -> {
                if (navRoute.user != null) currentUser = navRoute.user
            }
            is NavigationRoute.MedicationRoute -> {
                if (navRoute.user != null) currentUser = navRoute.user
            }
            NavigationRoute.LoginRoute -> {
                currentUser = null
            }
            else -> {}
        }

        _navState.update {
            it.copy(route = navRoute)
        }
    }

    fun showSnackBar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _snackBarState.value = SnackBarState(message, actionLabel, onAction)
    }

    fun dismissSnackBar() {
        _snackBarState.value = null
    }

    fun onBack() {
        when (navState.value.route) {
            NavigationRoute.ForgotPasswordRoute, NavigationRoute.RegisterRoute -> {
                updateNavigationState(NavigationRoute.LoginRoute)
            }

            else -> {
                updateNavigationState(NavigationRoute.HomeRoute(currentUser))
            }
        }
    }
}
