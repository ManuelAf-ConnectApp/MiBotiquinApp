package com.connectapp.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.connectapp.commonresources.back
import com.connectapp.commonresources.notifications
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    title: String, showBackButton: Boolean = false, notificationsEnabled: Boolean = true,
    onNavigationClick: () -> Unit = {}, onNotificationClick: () -> Unit = {}
) {
    Column {
        TopAppBar(
            title = { Text(title, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                if (showBackButton) IconButton(onClick = onNavigationClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(back))
                }
            },
            actions = {
                if (!showBackButton) IconButton(onClick = onNotificationClick) {
                    Icon(if (notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        stringResource(notifications))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.primary
            )
        )
        if (title.isNotBlank()) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
