package com.connectapp.data.datasource

import com.connectapp.data.model.MedicationDto

interface MedicationDataSource {
    suspend fun saveMedication(medication: MedicationDto)
    suspend fun getMedication(id: Long): MedicationDto?
    suspend fun getAllMedications(): List<MedicationDto>

    suspend fun getUserMedicationsByCodUser(codUser: String): List<MedicationDto>
}
