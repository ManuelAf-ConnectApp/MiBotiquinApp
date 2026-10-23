package com.connectapp.domain.validator

import com.connectapp.domain.validator.impl.FormValidatorImpl
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormValidatorImplTest {

    private val validator = FormValidatorImpl()

    @Test
    fun acceptsCommonEmails() {
        listOf("ana@test.com", "ana.prueba+farma@sub.dominio.es", "1ana@test.io", " ana@test.com ")
            .forEach { assertTrue(validator.isValidEmail(it), it) }
    }

    @Test
    fun rejectsMalformedEmails() {
        listOf("", "ana", "ana@", "@test.com", "ana@test", "ana@@test.com", "ana test@test.com", "ana@test.c", "a@b.c@d.com")
            .forEach { assertFalse(validator.isValidEmail(it), it) }
    }

    @Test
    fun passwordNeedsAtLeastEightCharacters() {
        assertFalse(validator.isValidPassword("1234567"))
        assertTrue(validator.isValidPassword("12345678"))
    }
}
