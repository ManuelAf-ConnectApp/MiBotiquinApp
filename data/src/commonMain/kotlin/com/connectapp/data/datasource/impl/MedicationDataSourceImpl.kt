package com.connectapp.data.datasource.impl

import com.connectapp.data.datasource.MedicationDataSource
import com.connectapp.data.database.dao.MedicationDao
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.mapper.toEntity
import com.connectapp.data.mapper.toDto
import com.connectapp.data.model.MedicationDto

class MedicationDataSourceImpl(
    private val medicationDao: MedicationDao
): MedicationDataSource {
    override suspend fun saveMedication(medication: MedicationDto) {
        medicationDao.insertMedication(medication.toDomain().toEntity())
    }

    override suspend fun getMedication(id: Long): MedicationDto? {
        return medicationDao.getMedicationById(id)?.toDomain()?.toDto()
    }

    override suspend fun getAllMedications(): List<MedicationDto> {
        return medicationDao.getAllMedicationsOnce().map { it.toDomain().toDto() }
    }

    override suspend fun getUserMedicationsByCodUser(codUser: String): List<MedicationDto> {
        return medicationDao.getUserMedicationsByCodUserOnce(codUser).map { it.toDomain().toDto() }
    }
}

