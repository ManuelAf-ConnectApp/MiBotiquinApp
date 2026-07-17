package com.connectapp.data.repository

import com.connectapp.data.datasource.MedicationDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.mapper.toDto
import com.connectapp.domain.model.AppError
import com.connectapp.domain.model.Medication
import com.connectapp.domain.repository.MedicationRepository

class MedicationRepositoryImpl(
    private val medicationDataSource: MedicationDataSource
): MedicationRepository {
    override suspend fun saveMedication(medication: Medication): Result<Unit> {
        return try {
            medicationDataSource.saveMedication(medication.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun getMedication(id: Long): Result<Medication?> {
        return try {
            val medication = medicationDataSource.getMedication(id)?.toDomain(id = id)
            Result.success(medication)
        } catch (e: Exception) {
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun getUserMedicationsByCodUser(codUser: String): Result<List<Medication>> {
        return try {
            val medications = medicationDataSource.getUserMedicationsByCodUser(codUser).map { it.toDomain() }
            Result.success(medications)
        } catch (e: Exception) {
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun getAllMedications(): Result<List<Medication>> {
        return try {
            val medications = medicationDataSource.getAllMedications().map { it.toDomain() }
            Result.success(medications)
        } catch (e: Exception) {
            Result.failure(AppError.UnknownError(e.message))
        }
    }
}
