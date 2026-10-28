package com.connectapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.connectapp.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // ABORT (no REPLACE): REPLACE borra la fila existente antes de insertar y, por el
    // ON DELETE CASCADE de medications, se llevaría por delante todos sus medicamentos.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("UPDATE users SET password = :passwordHash WHERE id = :id AND password = :expectedPassword")
    suspend fun upgradePasswordHash(id: Long, expectedPassword: String, passwordHash: String): Int

    // Compare-and-set: a concurrent password change invalidates issuance/reset.
    @Query("UPDATE users SET recoveryHash = :recoveryHash WHERE id = :id AND password = :expectedPassword")
    suspend fun setRecoveryHash(id: Long, expectedPassword: String, recoveryHash: String): Int

    // Single atomic statement: the same recovery code cannot be consumed twice.
    @Query("UPDATE users SET password = :passwordHash, recoveryHash = NULL, updatedAt = :updatedAt WHERE id = :id AND recoveryHash = :expectedRecoveryHash AND password = :expectedPassword")
    suspend fun consumeRecoveryCode(id: Long, expectedRecoveryHash: String, expectedPassword: String, passwordHash: String, updatedAt: String): Int

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)
}
