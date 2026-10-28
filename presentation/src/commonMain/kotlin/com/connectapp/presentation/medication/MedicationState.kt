package com.connectapp.presentation.medication

import com.connectapp.domain.model.DoseQuantity
import com.connectapp.domain.model.DoseUnit
import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User
import com.connectapp.domain.validator.MedicationRules
import com.connectapp.presentation.util.UiText

data class MedicationState(
    val id: Long = 0,
    val user: User? = null,
    val name: String = "",
    val dosesPerDay: Int = 1,
    val quantity: String = "",
    val quantityUnit: String = DoseUnit.TABLET.code,
    val durationDays: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isIndefinite: Boolean = false,
    val isPaused: Boolean = false,
    val notes: String = "",
    val pharmacologicalDose: String = "",
    val catalogueCode: String? = null,
    val catalogueName: String? = null,
    val catalogueRetrievedAt: Long? = null,
    val reminderMinutes: Int = 10,
    val startTimeHour: Int = 8,
    val startTimeMinute: Int = 0,
    val customSchedule: String = "",
    val weekdaySchedule: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val parsedQuantity: DoseQuantity?
        get() = DoseUnit.fromStored(quantityUnit)?.let { DoseQuantity.parse(quantity, it) }

    val isSaveButtonEnabled: Boolean
        get() = !isLoading && toMedication()?.let(MedicationRules::isValid) == true

    fun toMedication(): Medication? {
        val amount = parsedQuantity ?: return null
        val owner = user ?: return null
        return Medication(
            id = id, name = name, codUser = owner.id,
            dosesPerDay = dosesPerDay,
            quantity = amount.toDecimalString().toDouble(), // Compatibility mirror, never used for stock math.
            quantityMinor = amount.minorUnits, quantityUnit = amount.unit.code,
            durationDays = if (isIndefinite) 0 else MedicationRules.inclusiveDuration(startDate, endDate) ?: 0,
            startDate = startDate, endDate = endDate,
            reminderMinutes = reminderMinutes, startTimeHour = startTimeHour, startTimeMinute = startTimeMinute,
            customSchedule = customSchedule.trim(),
            weekdaySchedule = weekdaySchedule.trim(),
            isIndefinite = isIndefinite, isPaused = isPaused, notes = notes, pharmacologicalDose = pharmacologicalDose,
            catalogueCode = catalogueCode.takeIf { name == catalogueName },
            catalogueName = catalogueName.takeIf { name == catalogueName },
            catalogueRetrievedAt = catalogueRetrievedAt.takeIf { name == catalogueName }
        )
    }
}
