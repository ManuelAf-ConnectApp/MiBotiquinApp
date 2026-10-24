package com.connectapp.domain.validator

import com.connectapp.domain.model.Medication
import kotlin.test.*

class MedicationRulesTest {
    private val day = 86_400_000L
    private val medication = Medication(
        name = "Test", codUser = "1", dosesPerDay = 2, quantity = 0.5, quantityUnit = "tablet",
        durationDays = 3, startDate = day, endDate = 3 * day, reminderMinutes = 10,
        startTimeHour = 8, startTimeMinute = 0, quantityMinor = 500
    )

    @Test fun datesAreInclusiveAndNeverCorrectedSilently() {
        assertEquals(1, MedicationRules.inclusiveDuration(day, day))
        assertEquals(3, MedicationRules.inclusiveDuration(day, 3 * day))
        assertNull(MedicationRules.inclusiveDuration(day, 0))
        assertNull(MedicationRules.inclusiveDuration(null, day))
        assertNull(MedicationRules.inclusiveDuration(day + 1, 2 * day))
        assertTrue(MedicationRules.isValid(medication))
        assertFalse(MedicationRules.isValid(medication.copy(durationDays = 2)))
        assertFalse(MedicationRules.isValid(medication.copy(endDate = 0)))
    }

    @Test fun indefiniteTreatmentRequiresExplicitFlagAndNoEndDate() {
        assertTrue(MedicationRules.isValid(medication.copy(isIndefinite = true, endDate = null, durationDays = 0)))
        assertFalse(MedicationRules.isValid(medication.copy(endDate = null)))
        assertFalse(MedicationRules.isValid(medication.copy(isIndefinite = true)))
        assertFalse(MedicationRules.isValid(medication.copy(isIndefinite = true, startDate = null, endDate = null, durationDays = 0)))
    }

    @Test fun exactQuantityTakesPrecedenceAndInvalidExactValuesCannotFallBackToDouble() {
        assertEquals(500L, medication.copy(quantity = 999.0).doseQuantity?.minorUnits)
        assertFalse(MedicationRules.isValid(medication.copy(quantityMinor = 300)))
        assertFalse(MedicationRules.isValid(medication.copy(quantityUnit = "mg")))
        assertTrue(MedicationRules.isValid(medication.copy(quantityMinor = null, quantityUnit = "Pastillas")))
        assertFalse(MedicationRules.isValid(medication.copy(quantityMinor = null, quantity = 0.3)))
    }

    @Test fun malformedTimesFrequencyOrMissingIdentityAreRejected() {
        for (invalid in listOf(
            medication.copy(name = " "), medication.copy(codUser = ""),
            medication.copy(dosesPerDay = 0), medication.copy(dosesPerDay = 11),
            medication.copy(startTimeHour = 24), medication.copy(startTimeMinute = 60),
            medication.copy(reminderMinutes = -1), medication.copy(reminderMinutes = 1441)
        )) assertFalse(MedicationRules.isValid(invalid))
        assertTrue(MedicationRules.isValid(medication.copy(isPaused = true)))
    }
}
