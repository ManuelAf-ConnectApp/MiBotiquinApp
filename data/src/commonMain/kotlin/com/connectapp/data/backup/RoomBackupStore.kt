package com.connectapp.data.backup

import com.connectapp.data.database.entity.DoseEventEntity
import com.connectapp.data.database.entity.MedicationEntity
import com.connectapp.data.database.entity.StockAccountEntity
import com.connectapp.data.database.entity.StockMovementEntity
import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.data.datasource.DoseLocalDataSource
import kotlinx.serialization.json.Json

/** Exports and restores only the active account's health data, never account credentials. */
class RoomBackupStore(
    private val store: DoseLocalDataSource,
    private val session: AuthLocalDataSource,
    private val cipher: BackupCipher = BackupCipher(),
    private val json: Json = Json { ignoreUnknownKeys = false }
) {
    suspend fun export(passphrase: CharArray): String {
        val owner = owner()
        val plaintext = store.transaction {
            json.encodeToString(RoomBackupPayload(
                medications = store.medications.getUserMedicationsByCodUserOnce(owner).map { it.toBackup() },
                events = store.events.all(owner).map { it.toBackup() },
                accounts = store.stock.accounts(owner).map { it.toBackup() },
                movements = store.stock.allMovements(owner).map { it.toBackup() }
            )).encodeToByteArray()
        }
        return cipher.encrypt(plaintext, passphrase)
    }

    /** Restores only when the active account has no health records; it never overwrites data. */
    suspend fun restore(envelope: String, passphrase: CharArray) {
        val payload = json.decodeFromString<RoomBackupPayload>(cipher.decrypt(envelope, passphrase).decodeToString())
        require(payload.format == RoomBackupPayload.FORMAT) { "Unsupported backup data format." }
        require(payload.medications.map { it.id }.distinct().size == payload.medications.size) { "Invalid duplicate medication IDs." }
        require(payload.events.all { event -> payload.medications.any { it.id == event.medicationId } }) { "Invalid medication reference." }
        val owner = owner()
        store.transaction {
            require(store.medications.getUserMedicationsByCodUserOnce(owner).isEmpty()) { "The active account already contains data." }
            require(store.events.all(owner).isEmpty() && store.stock.accounts(owner).isEmpty() && store.stock.allMovements(owner).isEmpty()) { "The active account already contains data." }
            val medicationIds = payload.medications.associate { old ->
                old.id to store.medications.insertMedication(MedicationEntity(
                    name = old.name, codUser = owner, dosesPerDay = old.dosesPerDay, quantity = old.quantity,
                    quantityUnit = old.quantityUnit, durationDays = old.durationDays, startDate = old.startDate,
                    endDate = old.endDate, reminderMinutes = old.reminderMinutes, startTimeHour = old.startTimeHour,
                    startTimeMinute = old.startTimeMinute, quantityMinor = old.quantityMinor, isIndefinite = old.isIndefinite,
                    isPaused = old.isPaused, notes = old.notes, pharmacologicalDose = old.pharmacologicalDose,
                    catalogueCode = old.catalogueCode, catalogueName = old.catalogueName, catalogueRetrievedAt = old.catalogueRetrievedAt,
                    customSchedule = old.customSchedule, isArchived = old.isArchived
                ))
            }
            val eventIds = payload.events.associate { old ->
                old.id to "backup:$owner:${old.id}"
            }
            payload.events.forEach { old -> store.events.insert(DoseEventEntity(
                id = eventIds.getValue(old.id), medicationId = medicationIds.getValue(old.medicationId), codUser = owner,
                scheduledAt = old.scheduledAt, reminderAt = old.reminderAt, snoozedUntil = old.snoozedUntil, status = old.status, resolvedAt = old.resolvedAt,
                quantityMinor = old.quantityMinor, quantityUnit = old.quantityUnit, notes = old.notes, pharmacologicalDose = old.pharmacologicalDose
            )) }
            payload.accounts.forEach { old -> store.stock.insertAccount(StockAccountEntity(
                medicationId = medicationIds.getValue(old.medicationId), codUser = owner, quantityUnit = old.quantityUnit,
                initialMinor = old.initialMinor, balanceMinor = old.balanceMinor, lowThresholdMinor = old.lowThresholdMinor,
                createdAt = old.createdAt, updatedAt = old.updatedAt
            )) }
            payload.movements.forEach { old -> store.stock.insertMovement(StockMovementEntity(
                id = "backup:$owner:${old.id}", medicationId = medicationIds.getValue(old.medicationId), codUser = owner,
                kind = old.kind, deltaMinor = old.deltaMinor, requestedMinor = old.requestedMinor, quantityUnit = old.quantityUnit,
                createdAt = old.createdAt, reason = old.reason, eventId = old.eventId?.let(eventIds::get)
            )) }
        }
    }

    private fun owner() = session.getCurrentUserId()?.toString() ?: error("No active local account.")
}
