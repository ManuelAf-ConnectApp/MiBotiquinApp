package com.connectapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.connectapp.data.database.entity.DoseEventEntity

@Dao
interface DoseEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: DoseEventEntity): Long

    @Query("SELECT * FROM dose_events WHERE id = :id AND codUser = :owner")
    suspend fun get(id: String, owner: String): DoseEventEntity?

    @Query("SELECT * FROM dose_events WHERE codUser = :owner AND status = 'PENDING' ORDER BY scheduledAt LIMIT :limit")
    suspend fun pending(owner: String, limit: Int): List<DoseEventEntity>

    @Query("UPDATE dose_events SET status = :status, resolvedAt = :resolvedAt, quantityMinor = :quantityMinor, quantityUnit = :quantityUnit, notes = :notes, pharmacologicalDose = :pharmacologicalDose WHERE id = :id AND codUser = :owner AND status = 'PENDING'")
    suspend fun resolve(id: String, owner: String, status: String, resolvedAt: Long?, quantityMinor: Long, quantityUnit: String, notes: String, pharmacologicalDose: String): Int
}
