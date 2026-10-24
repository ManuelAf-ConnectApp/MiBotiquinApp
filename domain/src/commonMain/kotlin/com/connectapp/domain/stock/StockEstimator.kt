package com.connectapp.domain.stock

import com.connectapp.domain.model.*
import com.connectapp.domain.reminder.DoseSchedule
import com.connectapp.domain.validator.MedicationRules
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Forecast is the first future dose not covered, never an inferred actual consumption. */
object StockEstimator {
    fun estimate(medication: Medication, balanceMinor: Long?, now: Long, zone: TimeZone): StockForecast {
        if (balanceMinor == null) return StockForecast(StockForecastKind.NOT_CONFIGURED)
        if (medication.isPaused) return StockForecast(StockForecastKind.PAUSED)
        val amount = medication.doseQuantity
        if (balanceMinor < 0 || amount == null || !MedicationRules.isValid(medication)) {
            return StockForecast(StockForecastKind.UNKNOWN_SCHEDULE)
        }
        val covered = balanceMinor / amount.minorUnits
        val count = if (covered >= 3660) 3661 else covered.toInt() + 1
        // Ignore notification lead times; forecast actual dose times, including those whose alert already fired.
        val doses = DoseSchedule.upcomingDoses(medication.copy(reminderMinutes = 0), now, zone, count)
        if (doses.isEmpty()) return StockForecast(StockForecastKind.NO_FUTURE_DOSES)
        if (covered >= 3660) return StockForecast(
            if (!medication.isIndefinite && doses.size < count) StockForecastKind.ENOUGH_UNTIL_END else StockForecastKind.OUTSIDE_HORIZON)
        val firstUncovered = doses.getOrNull(covered.toInt())?.scheduledAt
        if (firstUncovered == null) return StockForecast(
            if (medication.isIndefinite) StockForecastKind.UNKNOWN_SCHEDULE else StockForecastKind.ENOUGH_UNTIL_END)
        val horizon = Instant.fromEpochMilliseconds(now).toLocalDateTime(zone).date.plus(365, DateTimeUnit.DAY)
        if (Instant.fromEpochMilliseconds(firstUncovered).toLocalDateTime(zone).date > horizon) {
            return StockForecast(StockForecastKind.OUTSIDE_HORIZON)
        }
        return StockForecast(StockForecastKind.ESTIMATED, firstUncovered)
    }
}
