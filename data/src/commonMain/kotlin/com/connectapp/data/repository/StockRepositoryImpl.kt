package com.connectapp.data.repository

import com.connectapp.data.database.entity.StockAccountEntity
import com.connectapp.data.database.entity.StockMovementEntity
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.DoseLocalDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.*
import com.connectapp.domain.repository.StockRepository
import com.connectapp.domain.stock.StockEstimator
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

class StockRepositoryImpl(private val store: DoseLocalDataSource, private val session: AuthLocalDataSource) : StockRepository {
    override suspend fun summaries(): Result<List<StockSummary>> = safely {
        val owner = owner()
        store.transaction {
            val now = Clock.System.now().toEpochMilliseconds()
            val result = store.medications.getUserMedicationsByCodUserOnce(owner).map { entity ->
                val med = entity.toDomain()
                val account = store.stock.account(med.id, owner)
                val unit = DoseUnit.fromStored(account?.quantityUnit ?: med.quantityUnit)
                val amount = med.doseQuantity
                val compatible = unit != null && amount?.unit == unit
                StockSummary(med.id, med.name, unit, account?.initialMinor, account?.balanceMinor,
                    account?.lowThresholdMinor, account?.createdAt,
                    if (account != null && !compatible) StockForecast(StockForecastKind.UNKNOWN_SCHEDULE)
                    else StockEstimator.estimate(med, account?.balanceMinor, now, TimeZone.currentSystemDefault()),
                    amount?.takeIf { compatible && !med.isPaused && med.dosesPerDay in 1..10 && it.minorUnits <= Long.MAX_VALUE / med.dosesPerDay }
                        ?.let { it.minorUnits * med.dosesPerDay })
            }
            requireOwner(owner)
            result
        }
    }

    override suspend fun movements(medicationId: Long): Result<List<StockMovement>> = safely {
        val owner = owner()
        store.transaction {
            medication(medicationId, owner)
            val result = store.stock.movements(medicationId, owner).map { movement ->
                StockMovement(movement.id, movement.medicationId, StockMovementKind.valueOf(movement.kind),
                    movement.deltaMinor, DoseUnit.fromStored(movement.quantityUnit) ?: throw AppError.StockUnitMismatch(),
                    movement.createdAt, movement.reason, movement.eventId)
            }
            requireOwner(owner)
            result
        }
    }

    override suspend fun configure(medicationId: Long, initialMinor: Long, thresholdMinor: Long, operationId: String): Result<Unit> = safely {
        val owner = owner()
        store.transaction {
            val med = medication(medicationId, owner)
            val unit = DoseUnit.fromStored(med.quantityUnit) ?: throw AppError.StockUnitMismatch()
            validate(initialMinor, unit); validate(thresholdMinor, unit); validateId(operationId)
            val existing = store.stock.movement(operationId, owner)
            if (existing != null) {
                validateReplay(existing, medicationId, StockMovementKind.INITIAL, initialMinor)
                if (store.stock.account(medicationId, owner)?.lowThresholdMinor != thresholdMinor) throw AppError.InvalidStock()
            } else {
                if (store.stock.account(medicationId, owner) != null) throw AppError.InvalidStock()
                val now = Clock.System.now().toEpochMilliseconds()
                store.stock.insertAccount(StockAccountEntity(medicationId, owner, unit.code, initialMinor, initialMinor, thresholdMinor, now, now))
                store.stock.insertMovement(StockMovementEntity(operationId, medicationId, owner, StockMovementKind.INITIAL.name,
                    initialMinor, initialMinor, unit.code, now, "", null))
            }
            requireOwner(owner)
        }
    }

    override suspend fun replenish(medicationId: Long, amountMinor: Long, reason: String, operationId: String) =
        mutate(medicationId, amountMinor, reason, operationId, StockMovementKind.REPLENISHMENT)

    override suspend fun adjust(medicationId: Long, targetMinor: Long, reason: String, operationId: String) =
        mutate(medicationId, targetMinor, reason, operationId, StockMovementKind.ADJUSTMENT)

    private suspend fun mutate(medicationId: Long, requested: Long, reason: String, operationId: String, kind: StockMovementKind): Result<Unit> = safely {
        val owner = owner()
        store.transaction {
            val med = medication(medicationId, owner)
            val account = store.stock.account(medicationId, owner) ?: throw AppError.InvalidStock()
            val unit = DoseUnit.fromStored(account.quantityUnit) ?: throw AppError.StockUnitMismatch()
            if (DoseUnit.fromStored(med.quantityUnit) != unit) throw AppError.StockUnitMismatch()
            validate(requested, unit); validateId(operationId)
            if (kind == StockMovementKind.ADJUSTMENT && reason.isBlank()) throw AppError.InvalidStock()
            if (kind == StockMovementKind.REPLENISHMENT && requested == 0L) throw AppError.InvalidStock()
            val existing = store.stock.movement(operationId, owner)
            if (existing != null) {
                validateReplay(existing, medicationId, kind, requested)
            } else {
                val balance = if (kind == StockMovementKind.REPLENISHMENT) {
                    if (requested > Long.MAX_VALUE - account.balanceMinor) throw AppError.InvalidStock()
                    account.balanceMinor + requested
                } else requested
                val delta = balance - account.balanceMinor
                if (delta == 0L) throw AppError.InvalidStock()
                val now = Clock.System.now().toEpochMilliseconds()
                if (store.stock.updateBalance(medicationId, owner, account.balanceMinor, balance, now) != 1) throw AppError.InvalidStock()
                store.stock.insertMovement(StockMovementEntity(operationId, medicationId, owner, kind.name,
                    delta, requested, unit.code, now, reason.trim(), null))
            }
            requireOwner(owner)
        }
    }

    override suspend fun setThreshold(medicationId: Long, thresholdMinor: Long): Result<Unit> = safely {
        val owner = owner()
        store.transaction {
            medication(medicationId, owner)
            val account = store.stock.account(medicationId, owner) ?: throw AppError.InvalidStock()
            val unit = DoseUnit.fromStored(account.quantityUnit) ?: throw AppError.StockUnitMismatch()
            validate(thresholdMinor, unit)
            if (store.stock.updateThreshold(medicationId, owner, thresholdMinor, Clock.System.now().toEpochMilliseconds()) != 1) throw AppError.InvalidStock()
            requireOwner(owner)
        }
    }

    private suspend fun medication(id: Long, owner: String) =
        store.medications.getMedicationById(id)?.takeIf { it.codUser == owner } ?: throw AppError.Unauthorized()
    private fun validate(value: Long, unit: DoseUnit) { if (!StockAmounts.valid(value, unit)) throw AppError.InvalidStock() }
    private fun validateId(id: String) { if (id.isBlank() || id.length > 128 || id.startsWith("dose:")) throw AppError.InvalidStock() }
    private fun validateReplay(existing: StockMovementEntity, medicationId: Long, kind: StockMovementKind, amount: Long) {
        if (existing.medicationId != medicationId || existing.kind != kind.name || existing.requestedMinor != amount) throw AppError.InvalidStock()
    }
    private fun owner() = session.getCurrentUserId()?.toString() ?: throw AppError.Unauthorized()
    private fun requireOwner(expected: String) { if (owner() != expected) throw AppError.Unauthorized() }
    private suspend fun <T> safely(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) { throw e
    } catch (e: AppError) { Result.failure(e)
    } catch (e: Exception) { Result.failure(AppError.UnknownError()) }
}
