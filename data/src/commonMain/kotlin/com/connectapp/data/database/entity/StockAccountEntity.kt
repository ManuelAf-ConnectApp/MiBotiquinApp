package com.connectapp.data.database.entity

import androidx.room.*

@Entity(tableName = "stock_accounts", foreignKeys = [ForeignKey(
    entity = MedicationEntity::class, parentColumns = ["id"], childColumns = ["medicationId"], onDelete = ForeignKey.CASCADE
)], indices = [Index(value = ["codUser"])])
data class StockAccountEntity(
    @PrimaryKey val medicationId: Long, val codUser: String, val quantityUnit: String,
    val initialMinor: Long, val balanceMinor: Long, val lowThresholdMinor: Long,
    val createdAt: Long, val updatedAt: Long
)
