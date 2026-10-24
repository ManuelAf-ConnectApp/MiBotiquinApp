package com.connectapp.domain.model

enum class StockMovementKind { INITIAL, REPLENISHMENT, ADJUSTMENT, DOSE }
enum class StockForecastKind { ESTIMATED, ENOUGH_UNTIL_END, NO_FUTURE_DOSES, PAUSED, UNKNOWN_SCHEDULE, OUTSIDE_HORIZON, NOT_CONFIGURED }

data class StockSummary(
    val medicationId: Long, val medicationName: String, val unit: DoseUnit?,
    val initialMinor: Long?, val balanceMinor: Long?, val lowThresholdMinor: Long?,
    val referenceAt: Long?, val forecast: StockForecast,
    val plannedDailyMinor: Long?
) {
    val configured get() = balanceMinor != null
    val isLow get() = balanceMinor != null && lowThresholdMinor != null && balanceMinor <= lowThresholdMinor
}

data class StockForecast(val kind: StockForecastKind, val firstUncoveredDoseAt: Long? = null)

data class StockMovement(
    val id: String, val medicationId: Long, val kind: StockMovementKind,
    val deltaMinor: Long, val unit: DoseUnit, val createdAt: Long, val reason: String, val eventId: String?
)

object StockAmounts {
    fun parse(text: String, unit: DoseUnit): Long? =
        if (text.trim().matches(Regex("0+(?:[.,]0+)?"))) 0L else DoseQuantity.parse(text, unit)?.minorUnits

    fun format(value: Long): String {
        val negative = value < 0
        require(value != Long.MIN_VALUE)
        val magnitude = if (negative) -value else value
        val whole = magnitude / DoseQuantity.SCALE
        val fraction = (magnitude % DoseQuantity.SCALE).toString().padStart(3, '0').trimEnd('0')
        return (if (negative) "-" else "") + whole + if (fraction.isEmpty()) "" else ".$fraction"
    }

    fun valid(value: Long, unit: DoseUnit) = value >= 0 && (unit != DoseUnit.TABLET || value % 250 == 0L)
}
