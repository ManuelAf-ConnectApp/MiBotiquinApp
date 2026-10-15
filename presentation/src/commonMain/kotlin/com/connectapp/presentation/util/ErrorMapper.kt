package com.connectapp.presentation.util

import com.connectapp.commonresources.*
import com.connectapp.domain.model.AppError
import com.connectapp.presentation.util.UiText.*

fun AppError.asUiText(): UiText {
    return when (this) {
        is AppError.NetworkError -> StringRes(error_network)
        is AppError.Unauthorized -> StringRes(error_unauthorized)
        is AppError.ServerError -> StringRes(error_server)
        is AppError.UnknownError -> StringRes(error_unknown)
        is AppError.CustomError -> DynamicString(message)
        is AppError.UserNotFound -> StringRes(error_user_not_found)
    }
}

fun Throwable.asUiText(): UiText {
    return if (this is AppError) {
        this.asUiText()
    } else {
        StringRes(error_unknown)
    }
}
