package com.connectapp.data.database.dao

import androidx.room.*
import com.connectapp.data.database.entity.StockAccountEntity
import com.connectapp.data.database.entity.StockMovementEntity

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_accounts WHERE medicationId = :medicationId AND codUser = :owner")
    suspend fun account(medicationId: Long, owner: String): StockAccountEntity?

    @Insert
    suspend fun insertAccount(account: StockAccountEntity)

    @Insert
    suspend fun insertMovement(movement: StockMovementEntity)

    @Query("SELECT * FROM stock_movements WHERE id = :id AND codUser = :owner")
    suspend fun movement(id: String, owner: String): StockMovementEntity?

    @Query("SELECT * FROM stock_movements WHERE medicationId = :medicationId AND codUser = :owner ORDER BY createdAt DESC, id DESC LIMIT 100")
    suspend fun movements(medicationId: Long, owner: String): List<StockMovementEntity>

    @Query("UPDATE stock_accounts SET balanceMinor = :balance, updatedAt = :updatedAt WHERE medicationId = :medicationId AND codUser = :owner AND balanceMinor = :expectedBalance")
    suspend fun updateBalance(medicationId: Long, owner: String, expectedBalance: Long, balance: Long, updatedAt: Long): Int

    @Query("UPDATE stock_accounts SET lowThresholdMinor = :threshold, updatedAt = :updatedAt WHERE medicationId = :medicationId AND codUser = :owner")
    suspend fun updateThreshold(medicationId: Long, owner: String, threshold: Long, updatedAt: Long): Int
}
