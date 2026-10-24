package com.connectapp.data.database.entity

import androidx.room.*

@Entity(tableName = "stock_movements", foreignKeys = [
    ForeignKey(entity = StockAccountEntity::class, parentColumns = ["medicationId"], childColumns = ["medicationId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = DoseEventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], deferred = true)
], indices = [Index(value = ["medicationId", "createdAt"]), Index(value = ["eventId"], unique = true)])
data class StockMovementEntity(
    @PrimaryKey val id: String, val medicationId: Long, val codUser: String,
    val kind: String, val deltaMinor: Long, val requestedMinor: Long,
    val quantityUnit: String, val createdAt: Long, val reason: String, val eventId: String?
)
