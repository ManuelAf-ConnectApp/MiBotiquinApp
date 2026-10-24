package com.connectapp.presentation.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.connectapp.commonresources.button_cancel
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun rememberCodeScanner(): CodeScanner {
    val cancelLabel = stringResource(button_cancel)
    return remember(cancelLabel) { IOSCodeScanner(cancelLabel) }
}
