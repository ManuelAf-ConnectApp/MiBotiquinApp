package com.connectapp.presentation.util

import com.connectapp.commonresources.*
import com.connectapp.domain.model.AppError

fun AppError.asUiText(): UiText {
    return when (this) {
        is AppError.NetworkError -> UiText.StringRes(error_network)
        is AppError.Unauthorized -> UiText.StringRes(error_unauthorized)
        is AppError.ServerError -> UiText.StringRes(error_server)
        is AppError.UnknownError -> UiText.StringRes(error_unknown)
        is AppError.CustomError -> UiText.DynamicString(message)
    }
}

fun Throwable.asUiText(): UiText {
    return if (this is AppError) {
        this.asUiText()
    } else {
        UiText.StringRes(error_unknown)
    }
}
