package com.connectapp.domain.catalogue

/** Only recognized Spanish identifiers. Never treat an arbitrary GTIN as a national code. */
object SpanishMedicationCode {
    fun nationalCode(raw: String): String? {
        if (raw.length > 4096) return null
        val value = raw.trim().removePrefix("]d2")
        if (value.matches(Regex("[0-9]{6}"))) return value
        if (value.length == 13 && value.startsWith("847000") && validCheckDigit(value)) return value.substring(6, 12)
        val ntin = when {
            value.matches(Regex("[0-9]{14}")) -> value
            value.startsWith("(01)") -> Regex("^\\(01\\)([0-9]{14})(?:\\(|$)").find(value)?.groupValues?.get(1)
            value.startsWith("01") && value.length >= 16 -> value.substring(2, 16)
            else -> null
        }
        return ntin?.takeIf { it.startsWith("0847000") && validCheckDigit(it) }?.substring(7, 13)
    }

    private fun validCheckDigit(value: String): Boolean {
        if (!value.all { it in '0'..'9' }) return false
        var weight = 3
        var sum = 0
        for (i in value.lastIndex - 1 downTo 0) {
            sum += value[i].digitToInt() * weight
            weight = 4 - weight
        }
        return (10 - sum % 10) % 10 == value.last().digitToInt()
    }
}
