package com.connectapp.domain.validator.impl

import com.connectapp.domain.validator.FormValidator

class FormValidatorImpl : FormValidator {

    private companion object {
        // Formato habitual usuario@dominio.tld: sin espacios, una sola @ y TLD de 2+ letras.
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    }

    override fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email.trim())
    }

    override fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
}