package com.connectapp.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Paleta extraída del logo de la app (navy + azul + teal).
val BrandNavy = Color(0xFF0B2E5C)
val BrandBlue = Color(0xFF0B57D0)
val BrandTeal = Color(0xFF0F8A8D)

val AppLightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E3F7),
    onPrimaryContainer = Color(0xFF0B2E5C),
    secondary = BrandTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFF0EC),
    onSecondaryContainer = Color(0xFF00201F),
    tertiary = BrandBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD8E2FF),
    onTertiaryContainer = Color(0xFF001A43),
)

val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF0B2E5C),
    primaryContainer = Color(0xFF123A6E),
    onPrimaryContainer = Color(0xFFD7E3F7),
    secondary = Color(0xFF7DD9D0),
    onSecondary = Color(0xFF00201F),
    secondaryContainer = Color(0xFF0C5049),
    onSecondaryContainer = Color(0xFFCFF0EC),
    tertiary = Color(0xFFAEC6FF),
    onTertiary = Color(0xFF001A43),
    tertiaryContainer = Color(0xFF00337A),
    onTertiaryContainer = Color(0xFFD8E2FF),
)
