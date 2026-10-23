package com.connectapp.domain.reminder

import com.connectapp.domain.model.Medication
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

// Cálculo puro (sin plataforma) de cuándo deben sonar los avisos de un medicamento, compartido
// por Android e iOS para que ambos programen exactamente las mismas horas.
object DoseSchedule {

    private const val MINUTES_PER_DAY = 24 * 60

    /**
     * Devuelve, en orden y como epoch millis, como máximo [limit] avisos posteriores a
     * [afterMillis]. Cada aviso es la hora de una dosis menos `reminderMinutes`.
     *
     * - Cada día de tratamiento tiene exactamente `dosesPerDay` dosis, repartidas cada
     *   24h / dosesPerDay a partir de la hora de inicio (en minutos, sin división entera de horas).
     *   El horario se re-ancla cada día, así que no acumula deriva.
     * - El tratamiento abarca del día de `startDate` al de `endDate`, ambos incluidos.
     *   Las dosis de un día que caen pasada la medianoche (p.ej. inicio a las 22:00) siguen
     *   perteneciendo a ese día de tratamiento.
     */
    fun upcomingReminders(
        medication: Medication,
        afterMillis: Long,
        timeZone: TimeZone,
        limit: Int,
    ): List<Long> {
        val dosesPerDay = medication.dosesPerDay
        if (dosesPerDay <= 0 || limit <= 0) return emptyList()

        val today = Instant.fromEpochMilliseconds(afterMillis).toLocalDateTime(timeZone).date
        val firstDay = medication.startDate?.let(::pickerDate) ?: today
        val lastDay = medication.endDate?.let(::pickerDate)
            ?: firstDay.plus(medication.durationDays.coerceAtLeast(1) - 1, DateTimeUnit.DAY)

        val doseTime = LocalTime(medication.startTimeHour, medication.startTimeMinute)
        val doseOffsets = (0 until dosesPerDay).map { (it * MINUTES_PER_DAY / dosesPerDay).minutes }
        val reminderLead = medication.reminderMinutes.coerceAtLeast(0).minutes

        // Las dosis de ayer pueden caer hoy (horarios que cruzan la medianoche), así que se
        // empieza un día antes; los días anteriores ya no pueden tener avisos pendientes.
        var day = maxOf(firstDay, today.minus(1, DateTimeUnit.DAY))
        val reminders = mutableListOf<Long>()
        while (day <= lastDay && reminders.size < limit) {
            val dayStart = LocalDateTime(day, doseTime).toInstant(timeZone)
            for (offset in doseOffsets) {
                val reminderMillis = (dayStart + offset - reminderLead).toEpochMilliseconds()
                if (reminderMillis > afterMillis) {
                    reminders += reminderMillis
                    if (reminders.size == limit) break
                }
            }
            day = day.plus(1, DateTimeUnit.DAY)
        }
        return reminders
    }

    // El DatePicker de Material 3 devuelve la medianoche UTC del día elegido: el día del
    // calendario que eligió el usuario es la fecha en UTC, no en la zona horaria local
    // (en zonas al oeste de UTC la zona local daría el día anterior).
    private fun pickerDate(millis: Long): LocalDate =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
}
