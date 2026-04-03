package com.sandeep.personalfinancecompanion.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val BUDGET_LIMIT = doublePreferencesKey("budget_limit")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val BUDGET_ALERTS_ENABLED = booleanPreferencesKey("budget_alerts_enabled")
        val GOAL_REMINDERS_ENABLED = booleanPreferencesKey("goal_reminders_enabled")
    }

    override val budgetLimitFlow: Flow<Double> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BUDGET_LIMIT] ?: 50000.0
        }

    override val currencyCodeFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE]
        }

    override val dailyReminderEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: false
        }

    override val reminderTimeFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] ?: "20:00"
        }

    override val budgetAlertsEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BUDGET_ALERTS_ENABLED] ?: true
        }

    override val goalRemindersEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GOAL_REMINDERS_ENABLED] ?: true
        }

    override suspend fun updateBudgetLimit(limit: Double) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDGET_LIMIT] = limit
        }
    }

    override suspend fun updateCurrencyCode(code: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE] = code
        }
    }

    override suspend fun updateDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
        }
    }

    override suspend fun updateReminderTime(time: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] = time
        }
    }

    override suspend fun updateBudgetAlertsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDGET_ALERTS_ENABLED] = enabled
        }
    }

    override suspend fun updateGoalRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOAL_REMINDERS_ENABLED] = enabled
        }
    }
}
