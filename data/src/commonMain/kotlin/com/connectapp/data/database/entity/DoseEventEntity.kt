package com.connectapp.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_events",
    foreignKeys = [ForeignKey(
        entity = MedicationEntity::class, parentColumns = ["id"], childColumns = ["medicationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["medicationId", "scheduledAt"], unique = true), Index(value = ["codUser", "status", "scheduledAt"])]
)
data class DoseEventEntity(
    @PrimaryKey val id: String,
    val medicationId: Long,
    val codUser: String,
    val scheduledAt: Long,
    val reminderAt: Long,
    @ColumnInfo(defaultValue = "NULL") val snoozedUntil: Long? = null,
    val status: String,
    val resolvedAt: Long?,
    val quantityMinor: Long,
    val quantityUnit: String,
    val notes: String,
    val pharmacologicalDose: String
)
