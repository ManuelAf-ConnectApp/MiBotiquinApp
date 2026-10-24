package com.connectapp.data.repository

import com.connectapp.data.datasource.impl.MedicationDataSourceImpl
import com.connectapp.data.mapper.toDomain
import com.connectapp.data.mapper.toDto
import com.connectapp.domain.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class StockRepositoryTest {
    private val after = 1_770_000_000_000L

    @Test fun initialStockIsExplicitAndDoesNotBackfillOldTakenDoses() = runBlocking<Unit> {
        val store = DoseRepositoryTest.Store(); val session = DoseRepositoryTest.Session()
        val stock = StockRepositoryImpl(store, session); val doses = DoseRepositoryImpl(store, session)
        assertNull(stock.summaries().getOrThrow().single().balanceMinor)
        val event = doses.planReminders(after, 1).getOrThrow().single()
        doses.resolveDose(event.eventId, DoseStatus.TAKEN).getOrThrow()
        assertTrue(store.stock.rows.isEmpty())
        stock.configure(1, 2000, 500, "initial").getOrThrow()
        stock.configure(1, 2000, 500, "initial").getOrThrow()
        doses.resolveDose(event.eventId, DoseStatus.TAKEN).getOrThrow()
        assertEquals(2000L, store.stock.accounts[1]?.balanceMinor)
        assertEquals(1, store.stock.rows.size)
    }

    @Test fun confirmedDoseDebitsOnceAndSkipOrOpeningNeverDebits() = runBlocking<Unit> {
        val store = DoseRepositoryTest.Store(); val session = DoseRepositoryTest.Session()
        val stock = StockRepositoryImpl(store, session); val doses = DoseRepositoryImpl(store, session)
        stock.configure(1, 2000, 500, "initial").getOrThrow()
        val events = doses.planReminders(after, 2).getOrThrow()
        doses.getDose(events[0].eventId).getOrThrow()
        assertEquals(2000L, store.stock.accounts[1]?.balanceMinor)
        (0..15).map { async { doses.resolveDose(events[0].eventId, DoseStatus.TAKEN).getOrThrow() } }.awaitAll()
        doses.resolveDose(events[1].eventId, DoseStatus.SKIPPED).getOrThrow()
        assertEquals(1500L, store.stock.accounts[1]?.balanceMinor)
        assertEquals(1, store.stock.rows.values.count { it.kind == StockMovementKind.DOSE.name })
        assertEquals(-500L, store.stock.rows["dose:${events[0].eventId}"]?.deltaMinor)
    }

    @Test fun insufficientStockLeavesDosePendingUntilAnExplicitReplenishment() = runBlocking<Unit> {
        val store = DoseRepositoryTest.Store(); val session = DoseRepositoryTest.Session()
        val stock = StockRepositoryImpl(store, session); val doses = DoseRepositoryImpl(store, session)
        stock.configure(1, 250, 500, "initial").getOrThrow()
        val event = doses.planReminders(after, 1).getOrThrow().single()
        assertIs<AppError.InsufficientStock>(doses.resolveDose(event.eventId, DoseStatus.TAKEN).exceptionOrNull())
        assertEquals(DoseStatus.PENDING, doses.getDose(event.eventId).getOrThrow()?.status)
        assertEquals(250L, store.stock.accounts[1]?.balanceMinor)
        stock.replenish(1, 1000, "New supply", "refill").getOrThrow()
        doses.resolveDose(event.eventId, DoseStatus.TAKEN).getOrThrow()
        assertEquals(750L, store.stock.accounts[1]?.balanceMinor)
    }

    @Test fun movementFailureRollsBackDoseAndBalanceTogether() = runBlocking<Unit> {
        val store = DoseRepositoryTest.Store(); val session = DoseRepositoryTest.Session()
        val stock = StockRepositoryImpl(store, session); val doses = DoseRepositoryImpl(store, session)
        stock.configure(1, 1000, 0, "initial").getOrThrow()
        val event = doses.planReminders(after, 1).getOrThrow().single()
        store.stock.failNextMovement = true
        assertTrue(doses.resolveDose(event.eventId, DoseStatus.TAKEN).isFailure)
        assertEquals(DoseStatus.PENDING, doses.getDose(event.eventId).getOrThrow()?.status)
        assertEquals(1000L, store.stock.accounts[1]?.balanceMinor)
        assertEquals(1, store.stock.rows.size)
        doses.resolveDose(event.eventId, DoseStatus.TAKEN).getOrThrow()
        assertEquals(500L, store.stock.accounts[1]?.balanceMinor)
    }

    @Test fun adjustmentsNeedReasonsAndOperationRetriesAreIdempotent() = runBlocking<Unit> {
        val store = DoseRepositoryTest.Store(); val session = DoseRepositoryTest.Session(); val stock = StockRepositoryImpl(store, session)
        stock.configure(1, 1000, 500, "initial").getOrThrow()
        stock.replenish(1, 500, "Supply", "refill").getOrThrow()
        stock.replenish(1, 500, "Supply", "refill").getOrThrow()
        assertIs<AppError.InvalidStock>(stock.adjust(1, 250, "", "adjust").exceptionOrNull())
        stock.adjust(1, 250, "Count corrected", "adjust").getOrThrow()
        stock.adjust(1, 250, "Count corrected", "adjust").getOrThrow()
        assertEquals(250L, store.stock.accounts[1]?.balanceMinor)
        assertEquals(-1250L, store.stock.rows["adjust"]?.deltaMinor)
        assertEquals(3, store.stock.rows.size)
        assertTrue(stock.summaries().getOrThrow().single().isLow)
        stock.setThreshold(1, 0).getOrThrow()
        assertFalse(stock.summaries().getOrThrow().single().isLow)
    }

    @Test fun accountsAndUnitsAreIsolatedAndOverflowCannotAlterBalance() = runBlocking<Unit> {
        val store = DoseRepositoryTest.Store(); val session = DoseRepositoryTest.Session(); val stock = StockRepositoryImpl(store, session)
        stock.configure(1, 1000, 0, "initial").getOrThrow()
        session.owner = 2
        assertTrue(stock.summaries().getOrThrow().isEmpty())
        assertIs<AppError.Unauthorized>(stock.movements(1).exceptionOrNull())
        assertIs<AppError.Unauthorized>(stock.replenish(1, 1000, "", "other").exceptionOrNull())
        session.owner = 1
        val source = MedicationDataSourceImpl(store)
        assertIs<AppError.StockUnitMismatch>(runCatching { source.saveMedication(store.meds[1]!!.toDomain().copy(quantityUnit = "ml").toDto()) }.exceptionOrNull())
        assertIs<AppError.StockIdentityChange>(runCatching { source.saveMedication(store.meds[1]!!.toDomain().copy(name = "Different product").toDto()) }.exceptionOrNull())
        assertEquals("tablet", store.meds[1]?.quantityUnit)
        assertIs<AppError.InvalidStock>(stock.replenish(1, -250, "", "negative").exceptionOrNull())
        assertIs<AppError.InvalidStock>(stock.replenish(1, 300, "", "fraction").exceptionOrNull())
        session.owner = null
        assertIs<AppError.Unauthorized>(stock.summaries().exceptionOrNull())
        val liquidStore = DoseRepositoryTest.Store(); liquidStore.meds[1] = liquidStore.meds[1]!!.copy(quantityUnit = "ml")
        val liquid = StockRepositoryImpl(liquidStore, DoseRepositoryTest.Session())
        liquid.configure(1, Long.MAX_VALUE, 0, "max").getOrThrow()
        assertIs<AppError.InvalidStock>(liquid.replenish(1, 1, "", "overflow").exceptionOrNull())
        assertEquals(Long.MAX_VALUE, liquidStore.stock.accounts[1]?.balanceMinor)
    }
}
