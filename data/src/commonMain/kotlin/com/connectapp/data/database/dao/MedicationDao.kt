package com.connectapp.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.connectapp.data.database.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Upsert
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Query("SELECT * FROM medications")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE codUser = :codUser AND isArchived = 0")
    fun getUserMedicationsByCodUser(codUser: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE codUser = :codUser AND isArchived = 0")
    suspend fun getUserMedicationsByCodUserOnce(codUser: String): List<MedicationEntity>

    @Query("SELECT * FROM medications")
    suspend fun getAllMedicationsOnce(): List<MedicationEntity>

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)

    @Query("UPDATE medications SET isArchived = 1, isPaused = 1 WHERE id = :id AND codUser = :owner AND isArchived = 0")
    suspend fun archiveMedication(id: Long, owner: String): Int
}
