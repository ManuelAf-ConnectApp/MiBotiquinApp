package com.connectapp.mibotiquinapp

import android.content.Intent
import com.connectapp.domain.notification.ReminderNavigation
import org.koin.android.ext.android.inject
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.presentation.theme.ThemeController
import org.koin.compose.koinInject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    private val reminderNavigation: ReminderNavigation by inject()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openReminder(intent)
    }

    private fun openReminder(intent: Intent?) {
        if (intent?.action != "com.connectapp.OPEN_DOSE") return
        val eventId = intent.getStringExtra("dose_event_id") ?: return
        reminderNavigation.open(eventId)
        intent.removeExtra("dose_event_id")
    }


    // Desde Android 13 POST_NOTIFICATIONS es un permiso en tiempo de ejecución: sin pedirlo,
    // los recordatorios se programan pero el sistema descarta las notificaciones.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        openReminder(intent)
        requestNotificationPermissionIfNeeded()

        setContent {
            val themeController = koinInject<ThemeController>()
            val dark by themeController.isDarkTheme.collectAsStateWithLifecycle()
            SideEffect {
                val style = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            App()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
