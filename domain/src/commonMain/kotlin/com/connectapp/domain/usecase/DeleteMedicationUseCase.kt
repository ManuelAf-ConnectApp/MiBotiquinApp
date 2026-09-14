package com.connectapp.domain.usecase

import com.connectapp.domain.repository.MedicationRepository

class DeleteMedicationUseCase(
    private val medicationRepository: MedicationRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return medicationRepository.deleteMedication(id)
    }
}
