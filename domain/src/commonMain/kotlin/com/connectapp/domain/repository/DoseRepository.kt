package com.connectapp.domain.repository

import com.connectapp.domain.model.DoseDetail
import com.connectapp.domain.model.DoseReminder
import com.connectapp.domain.model.DoseStatus

interface DoseRepository {
    suspend fun planReminders(afterMillis: Long, limit: Int = 60): Result<List<DoseReminder>>
    suspend fun getDose(eventId: String): Result<DoseDetail?>
    suspend fun resolveDose(eventId: String, status: DoseStatus): Result<DoseDetail?>
    suspend fun snoozeDose(eventId: String, minutes: Int = 10): Result<DoseDetail?>
    suspend fun pendingDoses(limit: Int = 20): Result<List<DoseDetail>>
    suspend fun doseHistory(limit: Int = 100): Result<List<DoseDetail>>
}
