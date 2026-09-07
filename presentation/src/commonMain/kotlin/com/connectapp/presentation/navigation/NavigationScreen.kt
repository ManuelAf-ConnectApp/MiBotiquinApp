package com.connectapp.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.component.ScreenTransitionIndicator
import com.connectapp.presentation.forgot_password.ForgotPasswordScreen
import com.connectapp.presentation.home.HomeScreen
import com.connectapp.domain.model.Medication
import com.connectapp.presentation.login.LoginScreen
import com.connectapp.presentation.medication.MedicationScreen
import com.connectapp.presentation.profile.ProfileScreen
import com.connectapp.presentation.register.RegisterScreen
import com.connectapp.presentation.settings.SettingsScreen
import com.connectapp.presentation.splash.SplashScreen
import kotlinx.coroutines.delay

private const val TRANSITION_DURATION_MS = 350

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

    var isTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(backStack.lastOrNull()) {
        isTransitioning = true
        delay(TRANSITION_DURATION_MS.toLong())
        isTransitioning = false
    }

    // Sin padding aquí: el `modifier` recibido ya trae el padding del Scaffold
    // (altura de la bottom bar) y cada pantalla lo aplica individualmente más abajo.
    // Aplicarlo también en este Box lo duplicaría.
    Box(modifier = Modifier.fillMaxSize()) {
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
            transitionSpec = {
                (fadeIn(animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> fullWidth / 6 }
                    )) togetherWith
                    fadeOut(animationSpec = tween(TRANSITION_DURATION_MS / 2))
            },
            popTransitionSpec = {
                (fadeIn(animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> -fullWidth / 6 }
                    )) togetherWith
                    fadeOut(animationSpec = tween(TRANSITION_DURATION_MS / 2))
            },
            predictivePopTransitionSpec = {
                (fadeIn(animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> -fullWidth / 6 }
                    )) togetherWith
                    fadeOut(animationSpec = tween(TRANSITION_DURATION_MS / 2))
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
                                onNavigateToAddMedication = {
                                    viewModel.updateNavigationState(NavigationRoute.MedicationRoute(user))
                                },
                                onNavigateToEditMedication = { medication ->
                                    viewModel.updateNavigationState(
                                        NavigationRoute.MedicationRoute(user, medication)
                                    )
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
                                medication = key.medication,
                                onNavigateBack = {
                                    viewModel.onBack()
                                },
                                onShowMessage = { message ->
                                    viewModel.showSnackBar(message = message)
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
                            },
                            onNavigateToProfile = {
                                viewModel.updateNavigationState(NavigationRoute.ProfileRoute())
                            },
                            onShowMessage = { message ->
                                viewModel.showSnackBar(message = message)
                            }
                        )
                    }
                }

                is NavigationRoute.ProfileRoute -> {
                    NavEntry(key = key) {
                        ProfileScreen(
                            modifier = modifier,
                            user = key.user,
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

        ScreenTransitionIndicator(
            visible = isTransitioning,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}