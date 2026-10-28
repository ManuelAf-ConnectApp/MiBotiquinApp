package com.connectapp.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["codUser"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["codUser"])]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val codUser: String,
    val dosesPerDay: Int,
    val quantity: Double,
    val quantityUnit: String,
    val durationDays: Int,
    val startDate: Long?,
    val endDate: Long?,
    val reminderMinutes: Int,
    val startTimeHour: Int,
    val startTimeMinute: Int,
    val quantityMinor: Long? = null,
    @ColumnInfo(defaultValue = "0") val isIndefinite: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isPaused: Boolean = false,
    @ColumnInfo(defaultValue = "''") val notes: String = "",
    @ColumnInfo(defaultValue = "''") val pharmacologicalDose: String = "",
    val catalogueCode: String? = null,
    val catalogueName: String? = null,
    val catalogueRetrievedAt: Long? = null,
    @ColumnInfo(defaultValue = "''") val customSchedule: String = "",
    @ColumnInfo(defaultValue = "''") val weekdaySchedule: String = "",
    @ColumnInfo(defaultValue = "0") val isArchived: Boolean = false,
)
