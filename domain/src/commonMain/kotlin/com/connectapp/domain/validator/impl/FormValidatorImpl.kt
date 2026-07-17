package com.connectapp.domain.validator.impl

import com.connectapp.domain.validator.FormValidator

class FormValidatorImpl : FormValidator {
    override fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
    }

    override fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
}