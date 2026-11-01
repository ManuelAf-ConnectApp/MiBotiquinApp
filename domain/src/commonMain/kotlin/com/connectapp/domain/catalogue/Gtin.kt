package com.connectapp.domain.catalogue

/** Validates GS1 GTIN-8/12/13/14 identifiers without assigning a medicine or a country. */
object Gtin {
    fun normalize(raw: String): String? {
        if (raw.length > 4096) return null
        val value = raw.trim().removePrefix("]d2")
        val digits = when {
            value.matches(Regex("[0-9]{8}|[0-9]{12,14}")) -> value
            value.startsWith("(01)") -> Regex("^\\(01\\)([0-9]{14})(?:\\(|$)").find(value)?.groupValues?.get(1)
            value.startsWith("01") && value.length >= 16 -> value.substring(2, 16)
            else -> null
        } ?: return null
        return digits.takeIf(::hasValidCheckDigit)
    }

    private fun hasValidCheckDigit(value: String): Boolean {
        var weight = 3
        var sum = 0
        for (index in value.lastIndex - 1 downTo 0) {
            sum += value[index].digitToInt() * weight
            weight = 4 - weight
        }
        return (10 - sum % 10) % 10 == value.last().digitToInt()
    }
}
