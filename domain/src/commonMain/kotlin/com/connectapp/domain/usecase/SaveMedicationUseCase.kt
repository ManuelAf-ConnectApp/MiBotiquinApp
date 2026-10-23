package com.connectapp.domain.usecase

import com.connectapp.domain.model.Medication
import com.connectapp.domain.repository.MedicationRepository

class SaveMedicationUseCase(
    private val medicationRepository: MedicationRepository,
    private val syncReminders: SyncRemindersUseCase
) {
    suspend operator fun invoke(medication: Medication): Result<Medication> {
        return medicationRepository.saveMedication(medication).onSuccess { syncReminders() }
    }
}
