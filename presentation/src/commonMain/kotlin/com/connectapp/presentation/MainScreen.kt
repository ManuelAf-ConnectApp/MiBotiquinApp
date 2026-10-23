package com.connectapp.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.main_title
import com.connectapp.commonresources.notifications_disabled_message
import com.connectapp.commonresources.notifications_enabled_message
import com.connectapp.presentation.component.CustomBottomBar
import com.connectapp.presentation.component.CustomTopBar
import com.connectapp.presentation.navigation.NavigationScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: MainViewModel = koinViewModel()
    val navState by viewModel.navState.collectAsStateWithLifecycle()
    val snackBarState by viewModel.snackBarState.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    val notificationsEnabledMessage = stringResource(notifications_enabled_message)
    val notificationsDisabledMessage = stringResource(notifications_disabled_message)

    val currentRoute = navState.route

    LaunchedEffect(snackBarState) {
        snackBarState?.let { state ->
            val result = snackBarHostState.showSnackbar(
                message = state.message,
                actionLabel = state.actionLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                state.onAction?.invoke()
            }
            viewModel.dismissSnackBar()
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth().imePadding().statusBarsPadding()
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                if (currentRoute.showTopBar) {
                    CustomTopBar(
                        showBackButton = currentRoute.showBackButton,
                        title = stringResource(main_title),
                        notificationsEnabled = notificationsEnabled,
                        onNavigationClick = {
                            viewModel.onBack()
                        },
                        onNotificationClick = {
                            viewModel.toggleNotifications()
                            viewModel.showSnackBar(
                                message = if (!notificationsEnabled) notificationsEnabledMessage else notificationsDisabledMessage
                            )
                        }
                    )
                }
            },
            bottomBar = {
                if (currentRoute.showBottomBar) {
                    CustomBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { viewModel.updateNavigationState(it) }
                    )
                }
            }
        ) { paddingValues ->
            NavigationScreen(
                modifier = modifier.padding(paddingValues = paddingValues),
                viewModel = viewModel
            )
        }
    }
}
