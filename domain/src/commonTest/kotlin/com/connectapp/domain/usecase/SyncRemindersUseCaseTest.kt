package com.connectapp.domain.usecase

import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.manager.PreferenceKeys
import com.connectapp.domain.manager.PreferenceStorage
import com.connectapp.domain.model.*
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.repository.DoseRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class SyncRemindersUseCaseTest {
    private class Session : AuthRepository {
        var owner: String? = "17"
        override suspend fun getSessionUserId() = owner
        override suspend fun login(email: String, password: String, rememberMe: Boolean): Result<User> = error("Unused")
        override suspend fun register(firstName: String, lastName: String, email: String, password: String): Result<User> = error("Unused")
        override suspend fun forgotPassword(email: String): Result<Unit> = error("Unused")
        override suspend fun getCurrentUser(): Result<User?> = error("Unused")
        override suspend fun restoreSession(): Result<User?> = error("Unused")
        override suspend fun logout(): Result<Unit> = error("Unused")
        override suspend fun deleteAccount(): Result<Unit> = error("Unused")
    }

    private class Preferences : PreferenceStorage {
        var enabled = true
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            assertEquals(PreferenceKeys.NOTIFICATIONS_ENABLED, key)
            return enabled
        }
        override fun getString(key: String): String? = error("Unused")
        override fun putString(key: String, value: String?) = error("Unused")
        override fun putBoolean(key: String, value: Boolean) = error("Unused")
        override fun remove(key: String) = error("Unused")
    }

    private class Notifications : NotificationManager {
        val scheduled = mutableListOf<List<DoseReminder>>()
        override fun scheduleReminders(reminders: List<DoseReminder>) { scheduled += reminders }
        override fun showNotification(title: String, body: String) = error("Unused")
        override fun diagnostics() = NotificationDiagnostics(true, true, 0, "test")
    }

    private class Doses : DoseRepository {
        var planCalls = 0
        var onPlan: suspend () -> Result<List<DoseReminder>> = { Result.success(listOf(reminder)) }
        override suspend fun planReminders(afterMillis: Long, limit: Int): Result<List<DoseReminder>> {
            planCalls++
            return onPlan()
        }
        override suspend fun getDose(eventId: String): Result<DoseDetail?> = error("Unused")
        override suspend fun resolveDose(eventId: String, status: DoseStatus): Result<DoseDetail?> = error("Unused")
        override suspend fun snoozeDose(eventId: String, minutes: Int): Result<DoseDetail?> = error("Unused")
        override suspend fun pendingDoses(limit: Int): Result<List<DoseDetail>> = error("Unused")
        override suspend fun doseHistory(limit: Int): Result<List<DoseDetail>> = error("Unused")
    }

    private class Scenario {
        val session = Session()
        val preferences = Preferences()
        val notifications = Notifications()
        val doses = Doses()
        val sync = SyncRemindersUseCase(session, doses, notifications, preferences)
    }

    @Test fun enabledSessionSchedulesThePersistedEventReferences() = runBlocking {
        val scenario = Scenario()
        scenario.sync()
        assertEquals(listOf(listOf(reminder)), scenario.notifications.scheduled)
    }

    @Test fun disabledNotificationsOrMissingSessionCancelExistingAlarmsWithoutPlanning() = runBlocking {
        for (loggedIn in listOf(true, false)) {
            val scenario = Scenario()
            if (loggedIn) scenario.preferences.enabled = false else scenario.session.owner = null
            scenario.sync()
            assertEquals(0, scenario.doses.planCalls)
            assertEquals(listOf(emptyList()), scenario.notifications.scheduled)
        }
    }

    @Test fun sessionChangedWhilePlanningCannotScheduleThePreviousAccountsEvents() = runBlocking {
        val scenario = Scenario()
        scenario.doses.onPlan = {
            scenario.session.owner = "18"
            Result.success(listOf(reminder))
        }
        scenario.sync()
        assertTrue(scenario.notifications.scheduled.isEmpty())
    }

    @Test fun disablingNotificationsWhilePlanningCancelsInsteadOfSchedulingTheOldResult() = runBlocking {
        val scenario = Scenario()
        scenario.doses.onPlan = {
            scenario.preferences.enabled = false
            Result.success(listOf(reminder))
        }
        scenario.sync()
        assertEquals(listOf(emptyList()), scenario.notifications.scheduled)
    }

    @Test fun disablingNotificationsStillCancelsWhenPlanningFails() = runBlocking {
        val scenario = Scenario()
        scenario.doses.onPlan = {
            scenario.preferences.enabled = false
            Result.failure(IllegalStateException("Temporary failure"))
        }
        scenario.sync()
        assertEquals(listOf(emptyList()), scenario.notifications.scheduled)
    }

    @Test fun transientPlanningFailureKeepsExistingAlarmsWhenStillEnabled() = runBlocking {
        val scenario = Scenario()
        scenario.doses.onPlan = { Result.failure(IllegalStateException("Temporary failure")) }
        scenario.sync()
        assertTrue(scenario.notifications.scheduled.isEmpty())
    }

    private companion object {
        val reminder = DoseReminder("9:1800000000000", 9, 1_800_000_000_000, 1_799_999_940_000)
    }
}
