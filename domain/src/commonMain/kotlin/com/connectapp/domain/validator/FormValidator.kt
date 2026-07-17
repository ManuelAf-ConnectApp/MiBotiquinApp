package com.connectapp.domain.validator

interface FormValidator {

    fun isValidEmail(email: String): Boolean

    fun isValidPassword(password: String): Boolean
}