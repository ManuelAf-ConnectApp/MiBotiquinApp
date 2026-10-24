package com.connectapp.data.mapper

import com.connectapp.domain.model.Medication
import kotlin.test.*

class MedicationPersistenceTest {
    @Test fun exactAmountAndTreatmentMetadataSurviveBothPersistencePaths() {
        val original = Medication(
            id = 17, name = "Test", codUser = "2", dosesPerDay = 2,
            quantity = 0.1, quantityUnit = "ml", quantityMinor = 100,
            durationDays = 0, startDate = 86_400_000, endDate = null,
            reminderMinutes = 0, startTimeHour = 8, startTimeMinute = 0,
            isIndefinite = true, isPaused = true, notes = "Saved instructions", pharmacologicalDose = "Prescribed amount",
            catalogueCode = "123456", catalogueName = "Test", catalogueRetrievedAt = 1_770_000_000_000L
        )
        assertEquals(original, original.toEntity().toDomain())
        assertEquals(original, original.toDto().toDomain())
        assertEquals(original, original.toDto().toDomain().toEntity().toDomain())
    }

    @Test fun unsupportedHistoricalQuantityIsPreservedWithoutGuessingAnExactAmount() {
        val original = Medication(
            id = 18, name = "Old", codUser = "2", dosesPerDay = 1,
            quantity = 0.3, quantityUnit = "Pastillas", durationDays = 1,
            startDate = null, endDate = null, reminderMinutes = 10, startTimeHour = 8, startTimeMinute = 0
        )
        val restored = original.toEntity().toDomain()
        assertEquals(original, restored)
        assertNull(restored.quantityMinor)
        assertNull(restored.doseQuantity)
    }
}
