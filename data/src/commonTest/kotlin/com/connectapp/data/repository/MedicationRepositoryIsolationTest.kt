package com.connectapp.data.repository

import com.connectapp.data.datasource.MedicationDataSource
import com.connectapp.data.model.MedicationDto
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.AppError
import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class MedicationRepositoryIsolationTest {
    private class Session(var userId: String? = "1") : AuthRepository {
        override suspend fun getSessionUserId() = userId
        override suspend fun login(email: String, password: String, rememberMe: Boolean): Result<User> = error("Unused")
        override suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<User> = error("Unused")
        override suspend fun forgotPassword(email: String): Result<Unit> = error("Unused")
        override suspend fun getCurrentUser(): Result<User?> = error("Unused")
        override suspend fun restoreSession(): Result<User?> = error("Unused")
        override suspend fun logout(): Result<Unit> = error("Unused")
        override suspend fun deleteAccount(): Result<Unit> = error("Unused")
    }

    private class Store : MedicationDataSource {
        val rows = mutableMapOf(
            1L to medication(1, "1"),
            2L to medication(2, "2")
        )
        var writes = 0
        override suspend fun getMedication(id: Long) = rows[id]
        override suspend fun getAllMedications() = rows.values.toList()
        override suspend fun getUserMedicationsByCodUser(codUser: String) = rows.values.filter { it.codUser == codUser }
        override suspend fun saveMedication(medication: MedicationDto): Long {
            writes++
            rows[medication.id] = medication
            return medication.id
        }
        override suspend fun deleteMedication(id: Long) { writes++; rows.remove(id) }
        override suspend fun archiveMedication(id: Long, owner: String): Boolean {
            val current = rows[id]?.takeIf { it.codUser == owner && !it.isArchived } ?: return false
            writes++; rows[id] = current.copy(isArchived = true, isPaused = true)
            return true
        }
    }

    @Test fun anotherAccountsMedicationCannotBeReadEditedOrDeleted() = runBlocking<Unit> {
        val store = Store()
        val repository = MedicationRepositoryImpl(store, Session())
        assertIs<AppError.Unauthorized>(repository.getMedication(2).exceptionOrNull())
        assertIs<AppError.Unauthorized>(repository.deleteMedication(2).exceptionOrNull())
        // Forging ownership on an existing id must not overwrite the other account.
        assertIs<AppError.Unauthorized>(repository.saveMedication(medication(2, "1").toDomain()).exceptionOrNull())
        assertIs<AppError.Unauthorized>(repository.saveMedication(medication(0, "2").toDomain()).exceptionOrNull())
        assertIs<AppError.Unauthorized>(repository.getUserMedicationsByCodUser("2").exceptionOrNull())
        assertEquals(0, store.writes)
        assertEquals("2", store.rows[2L]?.codUser)
    }

    @Test fun listingAndWritesFollowTheActiveSession() = runBlocking<Unit> {
        val session = Session()
        val store = Store()
        val repository = MedicationRepositoryImpl(store, session)
        assertEquals(listOf(1L), repository.getAllMedications().getOrThrow().map { it.id })
        assertTrue(repository.saveMedication(medication(1, "1").toDomain()).isSuccess)
        session.userId = "2"
        assertEquals(listOf(2L), repository.getAllMedications().getOrThrow().map { it.id })
        assertIs<AppError.Unauthorized>(repository.getMedication(1).exceptionOrNull())
        assertTrue(repository.deleteMedication(2).isSuccess)
        session.userId = null
        assertIs<AppError.Unauthorized>(repository.getAllMedications().exceptionOrNull())
        assertIs<AppError.Unauthorized>(repository.getMedication(1).exceptionOrNull())
        assertIs<AppError.Unauthorized>(repository.saveMedication(medication(1, "1").toDomain()).exceptionOrNull())
        assertIs<AppError.Unauthorized>(repository.deleteMedication(1).exceptionOrNull())
        assertEquals(2, store.writes)
    }

    @Test fun archivingOnlyAffectsTheActiveOwnersTreatment() = runBlocking<Unit> {
        val store = Store(); val repository = MedicationRepositoryImpl(store, Session())
        assertTrue(repository.archiveMedication(1).isSuccess)
        assertTrue(store.rows.getValue(1).isArchived)
        assertTrue(store.rows.getValue(1).isPaused)
        assertIs<AppError.Unauthorized>(repository.archiveMedication(2).exceptionOrNull())
    }

    companion object {
        private fun medication(id: Long, owner: String) = MedicationDto(
            id = id, name = "Test", dosesPerDay = 1, quantity = 1.0,
            quantityUnit = "pills", durationDays = 7, startDate = null, endDate = null,
            reminderMinutes = 0, startTimeHour = 9, startTimeMinute = 0, codUser = owner
        )
    }
}
