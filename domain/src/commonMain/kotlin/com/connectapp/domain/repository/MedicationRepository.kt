package com.connectapp.domain.repository

import com.connectapp.domain.model.Medication

interface MedicationRepository {
    suspend fun saveMedication(medication: Medication): Result<Unit>
    suspend fun getMedication(email: Long): Result<Medication?>

    suspend fun getUserMedicationsByCodUser(codUser: String): Result<List<Medication>>
    suspend fun getAllMedications(): Result<List<Medication>>
}
