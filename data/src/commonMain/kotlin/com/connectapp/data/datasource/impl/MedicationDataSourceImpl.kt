package com.connectapp.data.datasource.impl

import com.connectapp.data.datasource.MedicationDataSource
import com.connectapp.data.datasource.DoseLocalDataSource
import com.connectapp.domain.model.DoseUnit
import com.connectapp.domain.model.AppError
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.mapper.toEntity
import com.connectapp.data.mapper.toDto
import com.connectapp.data.model.MedicationDto

class MedicationDataSourceImpl(
    private val store: DoseLocalDataSource
): MedicationDataSource {
    private val medicationDao get() = store.medications
    override suspend fun saveMedication(medication: MedicationDto): Long {
        return store.transaction {
            val account = store.stock.account(medication.id, medication.codUser)
            if (account != null && DoseUnit.fromStored(account.quantityUnit) != DoseUnit.fromStored(medication.quantityUnit)) {
                throw AppError.StockUnitMismatch()
            }
            val previous = medicationDao.getMedicationById(medication.id)
            if (account != null && previous != null &&
                (!previous.name.trim().equals(medication.name.trim(), ignoreCase = true) || previous.catalogueCode != medication.catalogueCode)) {
                throw AppError.StockIdentityChange()
            }
            medicationDao.insertMedication(medication.toDomain().toEntity())
        }
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

    override suspend fun deleteMedication(id: Long) {
        medicationDao.deleteMedicationById(id)
    }

    override suspend fun archiveMedication(id: Long, owner: String): Boolean =
        medicationDao.archiveMedication(id, owner) == 1
}

