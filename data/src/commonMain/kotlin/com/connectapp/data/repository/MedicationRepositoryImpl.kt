package com.connectapp.data.repository

import com.connectapp.data.datasource.MedicationDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.mapper.toDto
import com.connectapp.domain.model.AppError
import com.connectapp.domain.model.Medication
import com.connectapp.domain.repository.MedicationRepository
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException

class MedicationRepositoryImpl(
    private val medicationDataSource: MedicationDataSource,
    private val authRepository: AuthRepository
): MedicationRepository {
    override suspend fun saveMedication(medication: Medication): Result<Medication> {
        return try {
            val userId = authRepository.getSessionUserId()
                ?: return Result.failure(AppError.Unauthorized())
            if (medication.codUser != userId) return Result.failure(AppError.Unauthorized())
            if (medication.id != 0L && medicationDataSource.getMedication(medication.id)?.codUser != userId) {
                return Result.failure(AppError.Unauthorized())
            }
            val generatedId = medicationDataSource.saveMedication(medication.toDto())
            val savedMedication = if (medication.id == 0L) medication.copy(id = generatedId) else medication
            Result.success(savedMedication)
        } catch (e: AppError) {
            Result.failure(e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun getMedication(id: Long): Result<Medication?> {
        return try {
            val userId = authRepository.getSessionUserId()
                ?: return Result.failure(AppError.Unauthorized())
            val medication = medicationDataSource.getMedication(id)?.toDomain(id = id)
            if (medication != null && medication.codUser != userId) return Result.failure(AppError.Unauthorized())
            Result.success(medication)
        } catch (e: AppError) {
            Result.failure(e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun getUserMedicationsByCodUser(codUser: String): Result<List<Medication>> {
        return try {
            if (authRepository.getSessionUserId() != codUser) return Result.failure(AppError.Unauthorized())
            val medications = medicationDataSource.getUserMedicationsByCodUser(codUser).map { it.toDomain() }
            Result.success(medications)
        } catch (e: AppError) {
            Result.failure(e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun getAllMedications(): Result<List<Medication>> {
        return try {
            val userId = authRepository.getSessionUserId()
                ?: return Result.failure(AppError.Unauthorized())
            val medications = medicationDataSource.getUserMedicationsByCodUser(userId).map { it.toDomain() }
            Result.success(medications)
        } catch (e: AppError) {
            Result.failure(e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun deleteMedication(id: Long): Result<Unit> {
        return try {
            val userId = authRepository.getSessionUserId()
                ?: return Result.failure(AppError.Unauthorized())
            if (medicationDataSource.getMedication(id)?.codUser != userId) return Result.failure(AppError.Unauthorized())
            medicationDataSource.deleteMedication(id)
            Result.success(Unit)
        } catch (e: AppError) {
            Result.failure(e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(AppError.UnknownError(e.message))
        }
    }

    override suspend fun archiveMedication(id: Long): Result<Unit> {
        return try {
            val userId = authRepository.getSessionUserId() ?: return Result.failure(AppError.Unauthorized())
            if (!medicationDataSource.archiveMedication(id, userId)) return Result.failure(AppError.Unauthorized())
            Result.success(Unit)
        } catch (e: AppError) {
            Result.failure(e)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(AppError.UnknownError(e.message))
        }
    }
}
