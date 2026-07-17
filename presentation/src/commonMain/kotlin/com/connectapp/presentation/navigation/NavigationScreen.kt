package com.connectapp.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.forgot_password.ForgotPasswordScreen
import com.connectapp.presentation.home.HomeScreen
import com.connectapp.presentation.login.LoginScreen
import com.connectapp.presentation.medication.MedicationScreen
import com.connectapp.presentation.profile.ProfileScreen
import com.connectapp.presentation.register.RegisterScreen
import com.connectapp.presentation.settings.SettingsScreen
import com.connectapp.presentation.splash.SplashScreen


@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
) {
    val backStack = remember { mutableStateListOf<Any>(NavigationRoute.SplashRoute) }

    val navState by viewModel.navState.collectAsState()

    LaunchedEffect(navState.route) {
        val newRoute = navState.route
        if (backStack.lastOrNull() != newRoute) {
            if (newRoute is NavigationRoute.HomeRoute || newRoute == NavigationRoute.SplashRoute) {
                backStack.clear()
                backStack.add(newRoute)
            } else {
                backStack.add(newRoute)
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLast()
                // Al volver atrás, informamos al ViewModel para que la UI (BottomBar) se actualice
                val previousRoute = backStack.last() as NavigationRoute
                viewModel.updateNavigationState(previousRoute)
            }
        },
        entryProvider = { key ->
            when (key) {
                is NavigationRoute.SplashRoute -> {
                    NavEntry(key = key) {
                        SplashScreen(
                            onNavigateToLogin = {
                                viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                            },
                            onNavigateToHome = { user ->
                                viewModel.updateNavigationState(NavigationRoute.HomeRoute(user))
                            }
                        )
                    }
                }

                is NavigationRoute.LoginRoute -> {
                    NavEntry(key = key) {
                        LoginScreen(
                            modifier = modifier,
                            onNavigateToRegister = {
                                viewModel.updateNavigationState(NavigationRoute.RegisterRoute)
                            },
                            onNavigateToForgotPassword = {
                                viewModel.updateNavigationState(NavigationRoute.ForgotPasswordRoute)
                            },
                            onNavigateToHome = { user ->
                                viewModel.updateNavigationState(NavigationRoute.HomeRoute(user))
                            }
                        )
                    }
                }

                is NavigationRoute.RegisterRoute -> {
                    NavEntry(key = key) {
                        RegisterScreen(
                            modifier = modifier,
                            onNavigateBack = {
                                viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                            },
                            onNavigateToHome = { user ->
                                viewModel.updateNavigationState(NavigationRoute.HomeRoute(user))
                            }
                        )
                    }
                }

                is NavigationRoute.ForgotPasswordRoute -> {
                    NavEntry(key = key) {
                        ForgotPasswordScreen(
                            modifier = modifier,
                            onNavigateBack = {
                                viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                            },
                        )
                    }
                }

                is NavigationRoute.HomeRoute -> {
                    NavEntry(key = key) {
                        key.user?.let { user ->
                            HomeScreen(
                                modifier = modifier,
                                user = user,
                                onShowToast = { item ->
                                    viewModel.showSnackBar(
                                        message = "You clicked on ${item.title}",
                                        actionLabel = "Close",
                                        onAction = {

                                        }
                                    )
                                },
                                onNavigateToAddMedication = {
                                    viewModel.updateNavigationState(NavigationRoute.MedicationRoute(user))
                                }
                            )
                        }

                    }
                }

                is NavigationRoute.MedicationRoute -> {
                    NavEntry(key = key) {
                        key.user?.let { user ->
                            MedicationScreen(
                                modifier = modifier,
                                user = user,
                                onNavigateBack = {
                                    viewModel.onBack()
                                }
                            )
                        }

                    }
                }

                is NavigationRoute.SettingsRoute -> {
                    NavEntry(key = key) {
                        SettingsScreen(
                            modifier = modifier,
                            onLogout = {
                                viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                            }
                        )
                    }
                }

                is NavigationRoute.ProfileRoute -> {
                    NavEntry(key = key) {
                        ProfileScreen(
                            modifier = modifier,
                            onNavigateBack = {
                                viewModel.onBack()
                            },
                            onNavigateToLogin = {
                                viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                            }
                        )
                    }
                }


                else -> {
                    NavEntry(key = key) {

                    }
                }
            }

        })

}