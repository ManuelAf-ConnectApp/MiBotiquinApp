package com.connectapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.SyncRemindersUseCase
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.navigation.NavigationState
import com.connectapp.presentation.navigation.SnackBarState
import com.connectapp.presentation.notification.NotificationsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val notificationsController: NotificationsController,
    private val syncReminders: SyncRemindersUseCase
) : ViewModel() {
    private val _navState = MutableStateFlow(NavigationState(route = NavigationRoute.SplashRoute))
    val navState: StateFlow<NavigationState> get() = _navState

    private val _snackBarState = MutableStateFlow<SnackBarState?>(null)
    val snackBarState: StateFlow<SnackBarState?> get() = _snackBarState

    val notificationsEnabled: StateFlow<Boolean> get() = notificationsController.notificationsEnabled

    private var currentUser: com.connectapp.domain.model.User? = null

    init {
        // Al abrir la app: reprograma tras cambios de zona horaria y, en iOS, rellena los
        // avisos pendientes (iOS sólo admite 64 y no hay forma de encadenarlos en segundo plano).
        viewModelScope.launch { syncReminders() }
    }

    fun updateNavigationState(navRoute: NavigationRoute) {
        val resolvedRoute = when (navRoute) {
            is NavigationRoute.HomeRoute -> {
                if (navRoute.user != null) {
                    currentUser = navRoute.user
                    navRoute
                } else {
                    // El origen de la navegación (p.ej. la bottom bar) no siempre conoce
                    // al usuario logueado: recuperamos el que ya tenemos guardado.
                    NavigationRoute.HomeRoute(currentUser)
                }
            }
            is NavigationRoute.MedicationRoute -> {
                if (navRoute.user != null) currentUser = navRoute.user
                navRoute
            }
            is NavigationRoute.ProfileRoute -> {
                if (navRoute.user != null) navRoute else NavigationRoute.ProfileRoute(currentUser)
            }
            NavigationRoute.LoginRoute -> {
                currentUser = null
                navRoute
            }
            else -> navRoute
        }

        _navState.update {
            it.copy(route = resolvedRoute)
        }
    }

    fun showSnackBar(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _snackBarState.value = SnackBarState(message, actionLabel, onAction)
    }

    fun dismissSnackBar() {
        _snackBarState.value = null
    }

    fun toggleNotifications() {
        notificationsController.toggle()
        viewModelScope.launch { syncReminders() }
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
