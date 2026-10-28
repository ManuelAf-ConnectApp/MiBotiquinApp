package com.connectapp.data.backup

import com.connectapp.data.database.entity.DoseEventEntity
import com.connectapp.data.database.entity.MedicationEntity
import com.connectapp.data.database.entity.StockAccountEntity
import com.connectapp.data.database.entity.StockMovementEntity
import kotlinx.serialization.Serializable

/** Plaintext inside EncryptedBackup. It deliberately excludes users, sessions and recovery data. */
@Serializable
data class RoomBackupPayload(
    val format: String = FORMAT,
    val medications: List<MedicationBackup>,
    val events: List<DoseEventBackup>,
    val accounts: List<StockAccountBackup>,
    val movements: List<StockMovementBackup>
) {
    companion object { const val FORMAT = "mibotiquin-room-data-v1" }
}

@Serializable data class MedicationBackup(val id: Long, val name: String, val dosesPerDay: Int, val quantity: Double, val quantityUnit: String, val durationDays: Int, val startDate: Long?, val endDate: Long?, val reminderMinutes: Int, val startTimeHour: Int, val startTimeMinute: Int, val quantityMinor: Long?, val isIndefinite: Boolean, val isPaused: Boolean, val notes: String, val pharmacologicalDose: String, val catalogueCode: String?, val catalogueName: String?, val catalogueRetrievedAt: Long?, val customSchedule: String = "", val isArchived: Boolean = false)
@Serializable data class DoseEventBackup(val id: String, val medicationId: Long, val scheduledAt: Long, val reminderAt: Long, val status: String, val resolvedAt: Long?, val quantityMinor: Long, val quantityUnit: String, val notes: String, val pharmacologicalDose: String, val snoozedUntil: Long? = null)
@Serializable data class StockAccountBackup(val medicationId: Long, val quantityUnit: String, val initialMinor: Long, val balanceMinor: Long, val lowThresholdMinor: Long, val createdAt: Long, val updatedAt: Long)
@Serializable data class StockMovementBackup(val id: String, val medicationId: Long, val kind: String, val deltaMinor: Long, val requestedMinor: Long, val quantityUnit: String, val createdAt: Long, val reason: String, val eventId: String?)

internal fun MedicationEntity.toBackup() = MedicationBackup(id, name, dosesPerDay, quantity, quantityUnit, durationDays, startDate, endDate, reminderMinutes, startTimeHour, startTimeMinute, quantityMinor, isIndefinite, isPaused, notes, pharmacologicalDose, catalogueCode, catalogueName, catalogueRetrievedAt, customSchedule, isArchived)
internal fun DoseEventEntity.toBackup() = DoseEventBackup(id, medicationId, scheduledAt, reminderAt, status, resolvedAt, quantityMinor, quantityUnit, notes, pharmacologicalDose, snoozedUntil)
internal fun StockAccountEntity.toBackup() = StockAccountBackup(medicationId, quantityUnit, initialMinor, balanceMinor, lowThresholdMinor, createdAt, updatedAt)
internal fun StockMovementEntity.toBackup() = StockMovementBackup(id, medicationId, kind, deltaMinor, requestedMinor, quantityUnit, createdAt, reason, eventId)
