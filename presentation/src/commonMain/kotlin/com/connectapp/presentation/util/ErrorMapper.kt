package com.connectapp.presentation.util

import com.connectapp.commonresources.*
import com.connectapp.domain.model.AppError
import com.connectapp.presentation.util.UiText.*

fun AppError.asUiText(): UiText {
    return when (this) {
        is AppError.InvalidMedicationCode -> StringRes(scan_unknown_code)
        is AppError.CatalogueUnavailable -> StringRes(error_catalogue_unavailable)
        is AppError.InvalidStock -> StringRes(error_invalid_stock)
        is AppError.InsufficientStock -> StringRes(error_insufficient_stock)
        is AppError.StockIdentityChange -> StringRes(error_stock_identity_change)
        is AppError.StockUnitMismatch -> StringRes(error_stock_unit_mismatch)
        is AppError.InvalidDose -> StringRes(dose_unavailable)
        is AppError.InvalidMedication -> StringRes(error_invalid_medication)
        is AppError.InvalidRecoveryCredentials -> StringRes(recovery_invalid)
        is AppError.RecoveryUnavailable -> StringRes(forgot_password_instruction)
        is AppError.NetworkError -> StringRes(error_network)
        is AppError.Unauthorized -> StringRes(error_unauthorized)
        is AppError.ServerError -> StringRes(error_server)
        is AppError.UnknownError -> StringRes(error_unknown)
        is AppError.CustomError -> DynamicString(message)
        is AppError.UserNotFound -> StringRes(error_user_not_found)
        is AppError.InvalidCredentials -> StringRes(error_invalid_credentials)
        is AppError.EmailAlreadyRegistered -> StringRes(error_email_already_registered)
    }
}

fun Throwable.asUiText(): UiText {
    return if (this is AppError) {
        this.asUiText()
    } else {
        StringRes(error_unknown)
    }
}
