package com.connectapp.data.repository

import com.connectapp.data.database.dao.StockDao
import com.connectapp.data.database.entity.StockAccountEntity
import com.connectapp.data.database.entity.StockMovementEntity

internal class FakeStockDao : StockDao {
    val accounts = mutableMapOf<Long, StockAccountEntity>()
    val rows = mutableMapOf<String, StockMovementEntity>()
    var failNextMovement = false
    override suspend fun account(medicationId: Long, owner: String) = accounts[medicationId]?.takeIf { it.codUser == owner }
    override suspend fun accounts(owner: String) = accounts.values.filter { it.codUser == owner }.sortedBy { it.medicationId }
    override suspend fun insertAccount(account: StockAccountEntity) { check(!accounts.containsKey(account.medicationId)); accounts[account.medicationId] = account }
    override suspend fun insertMovement(movement: StockMovementEntity) {
        if (failNextMovement) { failNextMovement = false; error("Simulated storage failure") }
        check(!rows.containsKey(movement.id))
        check(movement.eventId == null || rows.values.none { it.eventId == movement.eventId })
        rows[movement.id] = movement
    }
    override suspend fun movement(id: String, owner: String) = rows[id]?.takeIf { it.codUser == owner }
    override suspend fun allMovements(owner: String) = rows.values.filter { it.codUser == owner }.sortedWith(compareBy({ it.createdAt }, { it.id }))
    override suspend fun movements(medicationId: Long, owner: String) = rows.values.filter { it.medicationId == medicationId && it.codUser == owner }.sortedByDescending { it.createdAt }.take(100)
    override suspend fun updateBalance(medicationId: Long, owner: String, expectedBalance: Long, balance: Long, updatedAt: Long): Int {
        val row = account(medicationId, owner)?.takeIf { it.balanceMinor == expectedBalance } ?: return 0
        accounts[medicationId] = row.copy(balanceMinor = balance, updatedAt = updatedAt)
        return 1
    }
    override suspend fun updateThreshold(medicationId: Long, owner: String, threshold: Long, updatedAt: Long): Int {
        val row = account(medicationId, owner) ?: return 0
        accounts[medicationId] = row.copy(lowThresholdMinor = threshold, updatedAt = updatedAt)
        return 1
    }
}
