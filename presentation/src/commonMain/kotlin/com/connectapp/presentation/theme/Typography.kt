package com.connectapp.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

private val defaultTypography = Typography()

val AppTypography = defaultTypography.copy(
    displayLarge = defaultTypography.displayLarge.copy(fontSize = 48.sp, lineHeight = 56.sp),
    displayMedium = defaultTypography.displayMedium.copy(fontSize = 38.sp, lineHeight = 46.sp),
    displaySmall = defaultTypography.displaySmall.copy(fontSize = 30.sp, lineHeight = 38.sp),
    headlineLarge = defaultTypography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 34.sp),
    headlineMedium = defaultTypography.headlineMedium.copy(fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall = defaultTypography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = defaultTypography.titleLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = defaultTypography.titleMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
    titleSmall = defaultTypography.titleSmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
    bodyLarge = defaultTypography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = defaultTypography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 18.sp),
    bodySmall = defaultTypography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge = defaultTypography.labelLarge.copy(fontSize = 12.sp, lineHeight = 16.sp),
    labelMedium = defaultTypography.labelMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
    labelSmall = defaultTypography.labelSmall.copy(fontSize = 10.sp, lineHeight = 13.sp)
)
