package com.connectapp.mibotiquinapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.presentation.MainScreen
import com.connectapp.presentation.theme.AppDarkColorScheme
import com.connectapp.presentation.theme.AppLightColorScheme
import com.connectapp.presentation.theme.AppTypography
import com.connectapp.presentation.theme.AppShapes
import com.connectapp.presentation.theme.ThemeController
import org.koin.compose.koinInject

@Composable
fun App() {
    val themeController = koinInject<ThemeController>()
    val isDarkTheme by themeController.isDarkTheme.collectAsStateWithLifecycle()

    MaterialTheme(
        colorScheme = if (isDarkTheme) AppDarkColorScheme else AppLightColorScheme,
        typography = AppTypography,
        shapes = AppShapes
    ) {
        MainScreen()
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}