package com.connectapp.domain.stock

import com.connectapp.domain.model.*
import kotlinx.datetime.*
import kotlin.test.*

class StockEstimatorTest {
    private val zone = TimeZone.UTC
    private val start = LocalDate(2026, 1, 1).atStartOfDayIn(zone).toEpochMilliseconds()
    private val now = LocalDateTime(2026, 1, 1, 7, 55).toInstant(zone).toEpochMilliseconds()
    private val med = Medication(name = "Test", codUser = "1", dosesPerDay = 2, quantity = 0.25, quantityUnit = "tablet", quantityMinor = 250,
        durationDays = 0, startDate = start, endDate = null, isIndefinite = true, reminderMinutes = 10, startTimeHour = 8, startTimeMinute = 0)

    @Test fun forecastUsesDoseTimeRatherThanAlreadyDeliveredReminder() {
        assertEquals(LocalDateTime(2026, 1, 1, 8, 0).toInstant(zone).toEpochMilliseconds(), StockEstimator.estimate(med, 0, now, zone).firstUncoveredDoseAt)
        assertEquals(LocalDateTime(2026, 1, 3, 8, 0).toInstant(zone).toEpochMilliseconds(), StockEstimator.estimate(med, 1000, now, zone).firstUncoveredDoseAt)
    }
    @Test fun unknownPausedAndUnconfiguredStockNeverInventDates() {
        assertEquals(StockForecastKind.NOT_CONFIGURED, StockEstimator.estimate(med, null, now, zone).kind)
        assertEquals(StockForecastKind.PAUSED, StockEstimator.estimate(med.copy(isPaused = true), 1000, now, zone).kind)
        assertEquals(StockForecastKind.UNKNOWN_SCHEDULE, StockEstimator.estimate(med.copy(startDate = null), 1000, now, zone).kind)
        assertEquals(StockForecastKind.OUTSIDE_HORIZON, StockEstimator.estimate(med, Long.MAX_VALUE, now, zone).kind)
    }
    @Test fun finiteTreatmentCanEndBeforeStockIsExhausted() {
        val finite = med.copy(isIndefinite = false, endDate = start, durationDays = 1)
        assertEquals(StockForecastKind.ENOUGH_UNTIL_END, StockEstimator.estimate(finite, 500, now, zone).kind)
        assertEquals(StockForecastKind.ESTIMATED, StockEstimator.estimate(finite, 250, now, zone).kind)
        assertEquals(StockForecastKind.NO_FUTURE_DOSES, StockEstimator.estimate(finite, 0, now + 86_400_000L, zone).kind)
        assertEquals(StockForecastKind.ENOUGH_UNTIL_END, StockEstimator.estimate(finite, Long.MAX_VALUE, now, zone).kind)
    }
    @Test fun physicalQuantitiesAreExactAndZeroInventoryIsAllowed() {
        assertEquals(0L, StockAmounts.parse("0,000", DoseUnit.TABLET))
        assertEquals(750L, StockAmounts.parse("¾", DoseUnit.TABLET))
        assertNull(StockAmounts.parse("-1", DoseUnit.MILLILITER))
        assertEquals("0.001", StockAmounts.format(1))
        assertEquals("-0.25", StockAmounts.format(-250))
        assertEquals("9223372036854775.807", StockAmounts.format(Long.MAX_VALUE))
    }
}
