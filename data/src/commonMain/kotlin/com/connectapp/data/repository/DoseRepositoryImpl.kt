package com.connectapp.data.repository

import com.connectapp.data.datasource.DoseLocalDataSource
import com.connectapp.data.database.entity.StockMovementEntity
import com.connectapp.data.database.entity.DoseEventEntity
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.*
import com.connectapp.domain.reminder.DoseSchedule
import com.connectapp.domain.repository.DoseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

class DoseRepositoryImpl(private val store: DoseLocalDataSource, private val session: AuthLocalDataSource) : DoseRepository {
    private val events get() = store.events
    private val medications get() = store.medications

    override suspend fun planReminders(afterMillis: Long, limit: Int): Result<List<DoseReminder>> = safely {
        val owner = owner()
        store.transaction {
            val candidates = medications.getUserMedicationsByCodUserOnce(owner).flatMap { entity ->
                val med = entity.toDomain()
                val amount = med.doseQuantity ?: return@flatMap emptyList()
                DoseSchedule.upcomingDoses(med, afterMillis, TimeZone.currentSystemDefault(), limit.coerceIn(1, 60))
                    .map { occurrence -> med to DoseEventEntity(
                        id = "${med.id}:${occurrence.scheduledAt}", medicationId = med.id, codUser = owner,
                        scheduledAt = occurrence.scheduledAt, reminderAt = occurrence.reminderAt,
                        status = DoseStatus.PENDING.name, resolvedAt = null,
                        quantityMinor = amount.minorUnits, quantityUnit = amount.unit.code,
                        notes = med.notes, pharmacologicalDose = med.pharmacologicalDose
                    ) }
            }.sortedBy { it.second.reminderAt }
            val planned = mutableListOf<DoseReminder>()
            for ((_, event) in candidates) {
                if (planned.size >= limit.coerceIn(1, 60)) break
                events.insert(event)
                val stored = events.get(event.id, owner) ?: continue
                if (stored.status == DoseStatus.PENDING.name) planned += DoseReminder(event.id, event.medicationId, event.scheduledAt, event.reminderAt)
            }
            requireOwner(owner)
            planned
        }
    }

    override suspend fun getDose(eventId: String): Result<DoseDetail?> = safely {
        val owner = owner()
        store.transaction { detail(eventId, owner) }
    }

    override suspend fun resolveDose(eventId: String, status: DoseStatus): Result<DoseDetail?> = safely {
        if (status != DoseStatus.TAKEN && status != DoseStatus.SKIPPED) throw AppError.InvalidDose()
        val owner = owner()
        store.transaction {
            val current = detail(eventId, owner)
            if (current != null && current.status == DoseStatus.PENDING) {
                val account = store.stock.account(current.medicationId, owner)
                if (status == DoseStatus.TAKEN && account != null) {
                    if (DoseUnit.fromStored(account.quantityUnit) != current.quantity.unit) throw AppError.StockUnitMismatch()
                    if (account.balanceMinor < current.quantity.minorUnits) throw AppError.InsufficientStock()
                }
                val now = Clock.System.now().toEpochMilliseconds()
                val changed = events.resolve(eventId, owner, status.name, now,
                    current.quantity.minorUnits, current.quantity.unit.code, current.notes, current.pharmacologicalDose)
                if (changed == 1 && status == DoseStatus.TAKEN && account != null) {
                    val balance = account.balanceMinor - current.quantity.minorUnits
                    if (store.stock.updateBalance(current.medicationId, owner, account.balanceMinor, balance, now) != 1) throw AppError.InvalidStock()
                    store.stock.insertMovement(StockMovementEntity("dose:$eventId", current.medicationId, owner,
                        StockMovementKind.DOSE.name, -current.quantity.minorUnits, current.quantity.minorUnits,
                        current.quantity.unit.code, now, "", eventId))
                }
            }
            detail(eventId, owner)
        }
    }

    override suspend fun pendingDoses(limit: Int): Result<List<DoseDetail>> = safely {
        val owner = owner()
        store.transaction {
            events.pending(owner, limit.coerceIn(1, 60)).mapNotNull { event ->
                detail(event.id, owner)?.takeIf { it.status == DoseStatus.PENDING }
            }
        }
    }

    private suspend fun detail(id: String, owner: String): DoseDetail? {
        val event = events.get(id, owner) ?: return null
        val med = medications.getMedicationById(event.medicationId)?.toDomain() ?: return null
        if (med.codUser != owner) return null
        val status = runCatching { DoseStatus.valueOf(event.status) }.getOrNull() ?: return null
        val isPending = status == DoseStatus.PENDING
        // Re-read the current schedule. Deleted, paused, edited or timezone-obsolete reminders cannot be confirmed.
        val occurrence = if (isPending) DoseSchedule.upcomingDoses(
            med, event.scheduledAt - med.reminderMinutes.coerceAtLeast(0) * 60_000L - 1,
            TimeZone.currentSystemDefault(), 1
        ).firstOrNull() else null
        val currentAmount = med.doseQuantity
        val available = !isPending || (occurrence?.scheduledAt == event.scheduledAt && currentAmount != null)
        val amount = if (isPending && available) currentAmount else DoseUnit.fromStored(event.quantityUnit)?.let {
            DoseQuantity.fromMinorUnits(event.quantityMinor, it)
        }
        val account = store.stock.account(event.medicationId, owner)
        requireOwner(owner)
        return amount?.let { DoseDetail(
            event.id, event.medicationId, med.name, it, event.scheduledAt,
            occurrence?.reminderAt ?: event.reminderAt, if (available) status else DoseStatus.OBSOLETE,
            event.resolvedAt, if (isPending && available) med.notes else event.notes,
            if (isPending && available) med.pharmacologicalDose else event.pharmacologicalDose,
            account != null, account?.balanceMinor
        ) }
    }

    private fun owner() = session.getCurrentUserId()?.toString() ?: throw AppError.Unauthorized()
    private fun requireOwner(expected: String) {
        if (owner() != expected) throw AppError.Unauthorized()
    }
    private suspend fun <T> safely(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) { throw e
    } catch (e: AppError) { Result.failure(e)
    } catch (e: Exception) { Result.failure(AppError.UnknownError()) }
}
