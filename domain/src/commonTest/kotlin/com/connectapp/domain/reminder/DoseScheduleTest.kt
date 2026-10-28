package com.connectapp.domain.reminder

import com.connectapp.domain.model.Medication
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class DoseScheduleTest {

    private val utc = TimeZone.UTC

    // Igual que el DatePicker de Material 3: medianoche UTC del día elegido.
    private fun pickerDate(year: Int, month: Int, day: Int) =
        LocalDate(year, month, day).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    private fun millis(dateTime: String, timeZone: TimeZone = utc) =
        LocalDateTime.parse(dateTime).toInstant(timeZone).toEpochMilliseconds()

    private fun Long.asLocal(timeZone: TimeZone = utc) =
        Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).toString()

    private fun medication(
        dosesPerDay: Int,
        start: Long?,
        end: Long?,
        hour: Int = 8,
        minute: Int = 0,
        reminderMinutes: Int = 0,
        durationDays: Int = 1,
        customSchedule: String = "",
    ) = Medication(
        id = 1,
        name = "Test",
        codUser = "1",
        dosesPerDay = dosesPerDay,
        quantity = 1.0,
        quantityUnit = "Pastillas",
        durationDays = durationDays,
        startDate = start,
        endDate = end,
        reminderMinutes = reminderMinutes,
        startTimeHour = hour,
        startTimeMinute = minute,
        customSchedule = customSchedule,
    )

    @Test
    fun sevenDosesPerDayGiveExactlySevenRemindersPerDay() {
        val med = medication(dosesPerDay = 7, start = pickerDate(2026, 3, 2), end = pickerDate(2026, 3, 3))

        val reminders = DoseSchedule.upcomingReminders(med, millis("2026-03-01T00:00"), utc, limit = 100)

        assertEquals(14, reminders.size)
        assertEquals("2026-03-02T08:00", reminders.first().asLocal())
        assertEquals("2026-03-02T11:25", reminders[1].asLocal()) // 1440 / 7 = 205 min
        assertEquals("2026-03-03T08:00", reminders[7].asLocal()) // re-anclado cada día, sin deriva
    }

    @Test
    fun lastTreatmentDayIsIncluded() {
        val med = medication(dosesPerDay = 1, start = pickerDate(2026, 3, 2), end = pickerDate(2026, 3, 4))

        val reminders = DoseSchedule.upcomingReminders(med, millis("2026-03-01T00:00"), utc, limit = 100)

        assertEquals(
            listOf("2026-03-02T08:00", "2026-03-03T08:00", "2026-03-04T08:00"),
            reminders.map { it.asLocal() }
        )
    }

    @Test
    fun dosesAfterMidnightBelongToThePreviousTreatmentDay() {
        val med = medication(dosesPerDay = 3, start = pickerDate(2026, 3, 2), end = pickerDate(2026, 3, 2), hour = 22)

        val reminders = DoseSchedule.upcomingReminders(med, millis("2026-03-02T23:00"), utc, limit = 100)

        assertEquals(listOf("2026-03-03T06:00", "2026-03-03T14:00"), reminders.map { it.asLocal() })
    }

    @Test
    fun reminderLeadTimeIsSubtracted() {
        val med = medication(dosesPerDay = 1, start = pickerDate(2026, 3, 2), end = pickerDate(2026, 3, 2), reminderMinutes = 10)

        val reminders = DoseSchedule.upcomingReminders(med, millis("2026-03-01T00:00"), utc, limit = 1)

        assertEquals(listOf("2026-03-02T07:50"), reminders.map { it.asLocal() })
    }

    @Test
    fun pastRemindersAreSkippedAndFinishedTreatmentsHaveNone() {
        val med = medication(dosesPerDay = 2, start = pickerDate(2026, 3, 2), end = pickerDate(2026, 3, 3))

        val next = DoseSchedule.upcomingReminders(med, millis("2026-03-02T09:00"), utc, limit = 1)
        assertEquals(listOf("2026-03-02T20:00"), next.map { it.asLocal() })

        val afterEnd = DoseSchedule.upcomingReminders(med, millis("2026-03-03T20:00"), utc, limit = 10)
        assertTrue(afterEnd.isEmpty())
    }

    @Test
    fun pickedDayIsKeptInTimeZonesWestOfUtc() {
        val newYork = TimeZone.of("America/New_York")
        val med = medication(dosesPerDay = 1, start = pickerDate(2026, 3, 2), end = pickerDate(2026, 3, 2))

        val reminders = DoseSchedule.upcomingReminders(med, millis("2026-03-01T00:00", newYork), newYork, limit = 10)

        assertEquals(listOf("2026-03-02T08:00"), reminders.map { it.asLocal(newYork) })
    }

    @Test
    fun missingEndDateFallsBackToDuration() {
        val med = medication(dosesPerDay = 1, start = pickerDate(2026, 3, 2), end = null, durationDays = 2)

        val reminders = DoseSchedule.upcomingReminders(med, millis("2026-03-01T00:00"), utc, limit = 10)

        assertEquals(listOf("2026-03-02T08:00", "2026-03-03T08:00"), reminders.map { it.asLocal() })
    }
    @Test
    fun pausedTreatmentSchedulesNothingAndResumeKeepsFutureOnly() {
        val med = medication(2, pickerDate(2026, 3, 2), pickerDate(2026, 3, 3))
        val now = millis("2026-03-02T09:00")
        assertTrue(DoseSchedule.upcomingReminders(med.copy(isPaused = true), now, utc, 10).isEmpty())
        assertEquals("2026-03-02T20:00", DoseSchedule.upcomingReminders(med, now, utc, 1).single().asLocal())
    }

    @Test
    fun indefiniteTreatmentContinuesBeyondItsStartDayAndRespectsPlanningLimit() {
        val med = medication(1, pickerDate(2026, 3, 2), null, durationDays = 0).copy(isIndefinite = true)
        val reminders = DoseSchedule.upcomingReminders(med, millis("2026-04-01T00:00"), utc, 3)
        assertEquals(listOf("2026-04-01T08:00", "2026-04-02T08:00", "2026-04-03T08:00"), reminders.map { it.asLocal() })
    }

    @Test
    fun malformedScheduleDoesNotCrashNotificationSynchronization() {
        val med = medication(1, pickerDate(2026, 3, 2), null)
        for (invalid in listOf(med.copy(startTimeHour = 24), med.copy(startTimeMinute = -1), med.copy(dosesPerDay = Int.MAX_VALUE))) {
            assertTrue(DoseSchedule.upcomingReminders(invalid, millis("2026-03-01T00:00"), utc, 10).isEmpty())
        }
    }

    @Test
    fun customTimesReplaceEvenlySpacedDoses() {
        val med = medication(2, pickerDate(2026, 3, 2), pickerDate(2026, 3, 2), customSchedule = "07:30,19:15")
        assertEquals(listOf("2026-03-02T07:30", "2026-03-02T19:15"),
            DoseSchedule.upcomingReminders(med, millis("2026-03-01T00:00"), utc, 10).map { it.asLocal() })
    }

    @Test
    fun weekdayScheduleOnlyCreatesDosesOnConfiguredDays() {
        val med = medication(1, pickerDate(2026, 3, 2), pickerDate(2026, 3, 8)).copy(
            weekdaySchedule = "1=07:30|19:15;3=09:00;7=10:00"
        )
        assertEquals(
            listOf("2026-03-02T07:30", "2026-03-02T19:15", "2026-03-04T09:00", "2026-03-08T10:00"),
            DoseSchedule.upcomingReminders(med, millis("2026-03-01T00:00"), utc, 10).map { it.asLocal() }
        )
    }

}
