package com.connectapp.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private val defaultTypography = Typography()

val AppTypography = defaultTypography.copy(
    displayLarge = defaultTypography.displayLarge.copy(fontSize = 50.sp, lineHeight = 58.sp),
    displayMedium = defaultTypography.displayMedium.copy(fontSize = 40.sp, lineHeight = 48.sp),
    displaySmall = defaultTypography.displaySmall.copy(fontSize = 32.sp, lineHeight = 40.sp),
    headlineLarge = defaultTypography.headlineLarge.copy(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = defaultTypography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = defaultTypography.headlineSmall.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = defaultTypography.titleLarge.copy(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleMedium = defaultTypography.titleMedium.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = defaultTypography.titleSmall.copy(fontSize = 14.sp, lineHeight = 18.sp),
    bodyLarge = defaultTypography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = defaultTypography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = defaultTypography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = defaultTypography.labelLarge.copy(fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = defaultTypography.labelMedium.copy(fontSize = 13.sp, lineHeight = 16.sp),
    labelSmall = defaultTypography.labelSmall.copy(fontSize = 12.sp, lineHeight = 15.sp)
)
