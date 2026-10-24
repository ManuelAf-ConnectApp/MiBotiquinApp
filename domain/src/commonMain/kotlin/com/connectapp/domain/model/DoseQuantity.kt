package com.connectapp.domain.model

/** Stable storage codes, independent of the interface language. */
enum class DoseUnit(val code: String) {
    TABLET("tablet"), MILLILITER("ml");

    companion object {
        fun fromStored(value: String): DoseUnit? = when (value.trim().lowercase()) {
            "tablet", "tablets", "pills", "pastillas", "comprimidos" -> TABLET
            "ml", "milliliters", "mililitros" -> MILLILITER
            else -> null
        }
    }
}

/** Exact thousandths of the physical unit; tablets must be multiples of one quarter. */
@ConsistentCopyVisibility
data class DoseQuantity private constructor(val minorUnits: Long, val unit: DoseUnit) {
    init {
        require(minorUnits > 0)
        require(unit != DoseUnit.TABLET || minorUnits % 250L == 0L)
    }

    fun toDecimalString(): String {
        val whole = minorUnits / SCALE
        val fraction = (minorUnits % SCALE).toString().padStart(3, '0').trimEnd('0')
        return if (fraction.isEmpty()) whole.toString() else "$whole.$fraction"
    }

    fun toDisplayString(): String = if (unit == DoseUnit.TABLET) {
        when (minorUnits) {
            250L -> "¼"
            500L -> "½"
            750L -> "¾"
            else -> toDecimalString()
        }
    } else toDecimalString()

    companion object {
        const val SCALE = 1000L

        fun fromMinorUnits(value: Long, unit: DoseUnit): DoseQuantity? =
            if (value > 0 && (unit != DoseUnit.TABLET || value % 250L == 0L)) DoseQuantity(value, unit) else null

        fun parse(input: String, unit: DoseUnit): DoseQuantity? {
            val text = when (input.trim()) {
                "¼" -> if (unit == DoseUnit.TABLET) "0.25" else return null
                "½" -> if (unit == DoseUnit.TABLET) "0.5" else return null
                "¾" -> if (unit == DoseUnit.TABLET) "0.75" else return null
                else -> input.trim().replace(',', '.')
            }
            if (!text.matches(Regex("[0-9]+(?:\\.[0-9]+)?"))) return null
            val parts = text.split('.')
            val whole = parts[0].toLongOrNull() ?: return null
            val fraction = parts.getOrElse(1) { "" }.trimEnd('0')
            if (fraction.length > 3 || whole > Long.MAX_VALUE / SCALE) return null
            val remainder = fraction.padEnd(3, '0').toLongOrNull() ?: return null
            val scaled = whole * SCALE
            if (scaled > Long.MAX_VALUE - remainder) return null
            return fromMinorUnits(scaled + remainder, unit)
        }
    }
}
