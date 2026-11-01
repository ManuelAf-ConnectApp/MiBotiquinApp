package com.connectapp.domain.usecase

import com.connectapp.domain.model.AppError
import com.connectapp.domain.validator.MedicationRules
import com.connectapp.domain.model.Medication
import com.connectapp.domain.repository.MedicationRepository

class SaveMedicationUseCase(
    private val medicationRepository: MedicationRepository,
    private val syncReminders: SyncRemindersUseCase
) {
    suspend operator fun invoke(medication: Medication): Result<Medication> {
        if (!MedicationRules.isValid(medication)) return Result.failure(AppError.InvalidMedication())
        val amount = medication.doseQuantity ?: return Result.failure(AppError.InvalidMedication())
        val normalized = medication.copy(
            name = medication.name.trim(),
            quantityMinor = amount.minorUnits,
            quantityUnit = amount.unit.code,
            quantity = amount.toDecimalString().toDouble()
        )
        return medicationRepository.saveMedication(normalized).onSuccess { syncReminders() }
    }
}
