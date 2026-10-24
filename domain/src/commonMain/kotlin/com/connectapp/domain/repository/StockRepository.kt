package com.connectapp.domain.repository

import com.connectapp.domain.model.StockMovement
import com.connectapp.domain.model.StockSummary

interface StockRepository {
    suspend fun summaries(): Result<List<StockSummary>>
    suspend fun movements(medicationId: Long): Result<List<StockMovement>>
    suspend fun configure(medicationId: Long, initialMinor: Long, thresholdMinor: Long, operationId: String): Result<Unit>
    suspend fun replenish(medicationId: Long, amountMinor: Long, reason: String, operationId: String): Result<Unit>
    suspend fun adjust(medicationId: Long, targetMinor: Long, reason: String, operationId: String): Result<Unit>
    suspend fun setThreshold(medicationId: Long, thresholdMinor: Long): Result<Unit>
}
