package com.connectapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.connectapp.data.database.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Query("SELECT * FROM medications")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE codUser = :codUser")
    fun getUserMedicationsByCodUser(codUser: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE codUser = :codUser")
    suspend fun getUserMedicationsByCodUserOnce(codUser: String): List<MedicationEntity>

    @Query("SELECT * FROM medications")
    suspend fun getAllMedicationsOnce(): List<MedicationEntity>

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)
}
