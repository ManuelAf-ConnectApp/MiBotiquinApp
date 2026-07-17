package com.connectapp.presentation.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringRes(val resId: StringResource, val args: Array<Any> = arrayOf()) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringRes -> stringResource(resId, *args)
        }
    }
}
