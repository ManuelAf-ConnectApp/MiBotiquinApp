package com.connectapp.data.repository

import com.connectapp.data.database.dao.DoseEventDao
import com.connectapp.data.database.dao.MedicationDao
import com.connectapp.data.database.entity.DoseEventEntity
import com.connectapp.data.database.entity.MedicationEntity
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.DoseLocalDataSource
import com.connectapp.domain.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.test.*

class DoseRepositoryTest {
    internal class Session : AuthLocalDataSource {
        var owner: Long? = 1
        override fun getCurrentUserId() = owner
        override fun saveCurrentUserId(id: Long) { owner = id }
        override fun clearSession() { owner = null }
        override fun saveToken(token: String) = Unit
        override fun getToken(): String? = null
        override fun saveRememberMe(remember: Boolean) = Unit
        override fun isRememberMe() = false
    }
    internal class Store : DoseLocalDataSource {
        val rows = mutableMapOf<String, DoseEventEntity>()
        val meds = mutableMapOf(1L to MedicationEntity(1, "Test", "1", 2, 0.5, "tablet", 0,
            1_767_225_600_000L, null, 10, 8, 0, quantityMinor = 500, isIndefinite = true))
        var transitions = 0
        private val mutex = Mutex()
        override val stock = FakeStockDao()
        override suspend fun <T> transaction(block: suspend () -> T): T = mutex.withLock {
            val oldRows = rows.toMap(); val oldAccounts = stock.accounts.toMap(); val oldMovements = stock.rows.toMap(); val oldTransitions = transitions
            try { block() } catch (e: Throwable) {
                rows.clear(); rows.putAll(oldRows)
                stock.accounts.clear(); stock.accounts.putAll(oldAccounts)
                stock.rows.clear(); stock.rows.putAll(oldMovements)
                transitions = oldTransitions
                throw e
            }
        }
        override val events = object : DoseEventDao {
            override suspend fun insert(event: DoseEventEntity): Long {
                if (rows.containsKey(event.id)) return -1
                rows[event.id] = event
                return rows.size.toLong()
            }
            override suspend fun get(id: String, owner: String) = rows[id]?.takeIf { it.codUser == owner }
            override suspend fun all(owner: String) = rows.values.filter { it.codUser == owner }.sortedWith(compareBy({ it.scheduledAt }, { it.id }))
            override suspend fun pending(owner: String, limit: Int) = rows.values.filter { it.codUser == owner && it.status == "PENDING" }.sortedBy { it.scheduledAt }.take(limit)
            override suspend fun resolve(id: String, owner: String, status: String, resolvedAt: Long?, quantityMinor: Long, quantityUnit: String, notes: String, pharmacologicalDose: String): Int {
                val row = get(id, owner)?.takeIf { it.status == "PENDING" } ?: return 0
                transitions++
                rows[id] = row.copy(status = status, resolvedAt = resolvedAt, quantityMinor = quantityMinor, quantityUnit = quantityUnit, notes = notes, pharmacologicalDose = pharmacologicalDose)
                return 1
            }
            override suspend fun snooze(id: String, owner: String, reminderAt: Long): Int {
                val row = get(id, owner)?.takeIf { it.status == "PENDING" && (it.snoozedUntil ?: it.reminderAt) < reminderAt } ?: return 0
                rows[id] = row.copy(snoozedUntil = reminderAt)
                return 1
            }
        }
        override val medications = object : MedicationDao {
            override suspend fun insertMedication(medication: MedicationEntity): Long { meds[medication.id] = medication; return medication.id }
            override suspend fun getMedicationById(id: Long) = meds[id]
            override fun getAllMedications() = flowOf(meds.values.toList())
            override fun getUserMedicationsByCodUser(codUser: String) = flowOf(meds.values.filter { it.codUser == codUser && !it.isArchived })
            override suspend fun getUserMedicationsByCodUserOnce(codUser: String) = meds.values.filter { it.codUser == codUser && !it.isArchived }
            override suspend fun getAllMedicationsOnce() = meds.values.toList()
            override suspend fun deleteMedicationById(id: Long) { meds.remove(id); rows.entries.removeAll { it.value.medicationId == id } }
            override suspend fun archiveMedication(id: Long, owner: String): Int {
                val med = meds[id]?.takeIf { it.codUser == owner && !it.isArchived } ?: return 0
                meds[id] = med.copy(isArchived = true, isPaused = true)
                return 1
            }
        }
    }

    private val after = 1_770_000_000_000L

    @Test fun planningAndOpeningDoNotResolveAndRepeatedPlanningKeepsIdentity() = runBlocking<Unit> {
        val store = Store(); val repository = DoseRepositoryImpl(store, Session())
        val first = repository.planReminders(after, 3).getOrThrow()
        assertEquals(first, repository.planReminders(after, 3).getOrThrow())
        assertEquals(3, store.rows.size)
        val dose = repository.getDose(first.first().eventId).getOrThrow()!!
        assertEquals(DoseStatus.PENDING, dose.status)
        assertNull(dose.resolvedAt)
        assertEquals(0, store.transitions)
        assertEquals(500L, dose.quantity.minorUnits)
        assertEquals(dose.scheduledAt - 600_000L, dose.reminderAt)
    }

    @Test fun competingConfirmAndSkipRequestsOnlyResolveOnce() = runBlocking<Unit> {
        val store = Store(); val repository = DoseRepositoryImpl(store, Session())
        val id = repository.planReminders(after, 1).getOrThrow().single().eventId
        val results = (0..15).map { index -> async {
            repository.resolveDose(id, if (index % 2 == 0) DoseStatus.TAKEN else DoseStatus.SKIPPED).getOrThrow()!!
        } }.awaitAll()
        assertEquals(1, store.transitions)
        assertTrue(results.all { it.status == results.first().status && it.resolvedAt == results.first().resolvedAt })
        assertTrue(repository.planReminders(after, 1).getOrThrow().none { it.eventId == id })
    }

    @Test fun anotherAccountOrMissingSessionCannotReadOrResolve() = runBlocking<Unit> {
        val store = Store(); val session = Session(); val repository = DoseRepositoryImpl(store, session)
        val id = repository.planReminders(after, 1).getOrThrow().single().eventId
        session.owner = 2
        assertNull(repository.getDose(id).getOrThrow())
        assertNull(repository.resolveDose(id, DoseStatus.TAKEN).getOrThrow())
        assertTrue(repository.pendingDoses().getOrThrow().isEmpty())
        session.owner = null
        assertIs<AppError.Unauthorized>(repository.getDose(id).exceptionOrNull())
        assertIs<AppError.Unauthorized>(repository.resolveDose(id, DoseStatus.TAKEN).exceptionOrNull())
        assertEquals(0, store.transitions)
    }

    @Test fun currentMedicationIsReloadedAndPausedOrChangedSchedulesBecomeObsolete() = runBlocking<Unit> {
        val store = Store(); val repository = DoseRepositoryImpl(store, Session())
        val id = repository.planReminders(after, 1).getOrThrow().single().eventId
        store.meds[1] = store.meds[1]!!.copy(name = "Updated", quantityMinor = 750, notes = "Updated notes")
        val updated = repository.getDose(id).getOrThrow()!!
        assertEquals("Updated", updated.medicationName)
        assertEquals(750L, updated.quantity.minorUnits)
        assertEquals("Updated notes", updated.notes)
        store.meds[1] = store.meds[1]!!.copy(isPaused = true)
        assertEquals(DoseStatus.OBSOLETE, repository.resolveDose(id, DoseStatus.TAKEN).getOrThrow()?.status)
        store.meds[1] = store.meds[1]!!.copy(isPaused = false, startTimeMinute = 7)
        assertEquals(DoseStatus.OBSOLETE, repository.getDose(id).getOrThrow()?.status)
        assertEquals(0, store.transitions)
        store.medications.deleteMedicationById(1)
        assertNull(repository.getDose(id).getOrThrow())
    }

    @Test fun resolvedQuantityAndNotesRemainHistoricalAfterEditingTreatment() = runBlocking<Unit> {
        val store = Store(); val repository = DoseRepositoryImpl(store, Session())
        val id = repository.planReminders(after, 1).getOrThrow().single().eventId
        val recorded = repository.resolveDose(id, DoseStatus.TAKEN).getOrThrow()!!
        store.meds[1] = store.meds[1]!!.copy(quantityMinor = 1000, notes = "New instructions", isPaused = true)
        val reopened = repository.getDose(id).getOrThrow()!!
        assertEquals(recorded.quantity, reopened.quantity)
        assertEquals(recorded.notes, reopened.notes)
        assertEquals(DoseStatus.TAKEN, reopened.status)
        assertIs<AppError.InvalidDose>(repository.resolveDose(id, DoseStatus.PENDING).exceptionOrNull())
    }

    @Test fun snoozingPersistsTheEffectiveReminderWithoutResolvingOrChangingStock() = runBlocking<Unit> {
        val store = Store(); val repository = DoseRepositoryImpl(store, Session())
        val id = repository.planReminders(after, 1).getOrThrow().single().eventId
        val before = repository.getDose(id).getOrThrow()!!
        val snoozed = repository.snoozeDose(id, 10).getOrThrow()!!
        assertEquals(DoseStatus.PENDING, snoozed.status)
        assertEquals(0, store.transitions)
        assertTrue(snoozed.reminderAt >= before.reminderAt)
        assertEquals(snoozed.reminderAt, store.rows.getValue(id).snoozedUntil)
        assertEquals(snoozed.reminderAt, repository.planReminders(after, 1).getOrThrow().single().reminderAt)
    }

    @Test fun resolvedDosesRemainInTheAccountHistory() = runBlocking<Unit> {
        val store = Store(); val repository = DoseRepositoryImpl(store, Session())
        val events = repository.planReminders(after, 2).getOrThrow()
        repository.resolveDose(events[0].eventId, DoseStatus.TAKEN).getOrThrow()
        repository.resolveDose(events[1].eventId, DoseStatus.SKIPPED).getOrThrow()
        assertEquals(setOf(DoseStatus.TAKEN, DoseStatus.SKIPPED), repository.doseHistory().getOrThrow().map { it.status }.toSet())
    }
}
