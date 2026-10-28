package com.connectapp.data.datasource

import com.connectapp.data.model.MedicationDto

interface MedicationDataSource {
    suspend fun saveMedication(medication: MedicationDto): Long
    suspend fun getMedication(id: Long): MedicationDto?
    suspend fun getAllMedications(): List<MedicationDto>
    suspend fun getUserMedicationsByCodUser(codUser: String): List<MedicationDto>
    suspend fun deleteMedication(id: Long)
    suspend fun archiveMedication(id: Long, owner: String): Boolean
}
