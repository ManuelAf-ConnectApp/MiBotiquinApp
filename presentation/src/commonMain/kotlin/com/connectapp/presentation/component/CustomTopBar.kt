package com.connectapp.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.connectapp.commonresources.back
import com.connectapp.commonresources.menu
import com.connectapp.commonresources.notifications
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    title: String,
    showBackButton: Boolean = false,
    notificationsEnabled: Boolean = true,
    onNavigationClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = if (showBackButton) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                    contentDescription = if (showBackButton) stringResource(back) else stringResource(menu)
                )
            }
        },
        actions = {
            if (!showBackButton) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = if (notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = stringResource(notifications)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
