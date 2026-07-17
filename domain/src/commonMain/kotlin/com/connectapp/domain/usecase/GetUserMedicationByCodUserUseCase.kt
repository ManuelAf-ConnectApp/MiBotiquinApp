package com.connectapp.domain.usecase

import com.connectapp.domain.model.Medication
import com.connectapp.domain.repository.MedicationRepository

class GetUserMedicationByCodUserUseCase(
    private val medicationRepository: MedicationRepository
) {
    suspend operator fun invoke(codUser: String): Result<List<Medication>> {
        return medicationRepository.getUserMedicationsByCodUser(codUser)
    }
}