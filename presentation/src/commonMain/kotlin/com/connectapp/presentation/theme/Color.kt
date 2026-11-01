package com.connectapp.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Screenshot direction: petrol teal, near-white surfaces and warm charcoal text.
val BrandTeal = Color(0xFF006B6B)
val BrandNavy = Color(0xFF173F43)
val BrandBlue = Color(0xFF416778)

val AppLightColorScheme = lightColorScheme(
    primary = BrandTeal, onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F0EF), onPrimaryContainer = Color(0xFF004F50),
    secondary = Color(0xFF4E6364), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDF4F3), onSecondaryContainer = Color(0xFF344B4C),
    tertiary = BrandBlue, onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE2EFF5), onTertiaryContainer = Color(0xFF204451),
    background = Color(0xFFFAFDFD), onBackground = Color(0xFF191E1E),
    surface = Color(0xFFFAFDFD), onSurface = Color(0xFF191E1E),
    surfaceVariant = Color(0xFFEDF4F3), onSurfaceVariant = Color(0xFF4E6364),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF3F8F7),
    surfaceContainer = Color(0xFFEDF4F3), surfaceContainerHigh = Color(0xFFE7F0EF),
    surfaceContainerHighest = Color(0xFFE0EBEA),
    outline = Color(0xFF6E7B7B), outlineVariant = Color(0xFFD6E1E0),
    error = Color(0xFFB3261E), onError = Color.White,
    errorContainer = Color(0xFFFCE9E7), onErrorContainer = Color(0xFF8C1D18)
)

val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFF80D5D1), onPrimary = Color(0xFF003737),
    primaryContainer = Color(0xFF164D4D), onPrimaryContainer = Color(0xFFA2F0EB),
    secondary = Color(0xFFB0CCCC), onSecondary = Color(0xFF183333),
    secondaryContainer = Color(0xFF304B4B), onSecondaryContainer = Color(0xFFD4E8E7),
    tertiary = Color(0xFFABCEDC), onTertiary = Color(0xFF143441),
    background = Color(0xFF101919), onBackground = Color(0xFFE2EBE9),
    surface = Color(0xFF101919), onSurface = Color(0xFFE2EBE9),
    surfaceVariant = Color(0xFF293B3A), onSurfaceVariant = Color(0xFFB7CBC9),
    surfaceContainerLowest = Color(0xFF0B1414), surfaceContainerLow = Color(0xFF162221),
    surfaceContainer = Color(0xFF1B2928), surfaceContainerHigh = Color(0xFF243331),
    surfaceContainerHighest = Color(0xFF2E3E3B),
    outline = Color(0xFF8B9F9D), outlineVariant = Color(0xFF405450),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF63302E), onErrorContainer = Color(0xFFFFDAD6)
)
