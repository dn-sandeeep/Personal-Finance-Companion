package com.sandeep.personalfinancecompanion.domain.repository

import com.sandeep.personalfinancecompanion.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val budgetLimitFlow: Flow<Double>
    val currencyFlow: Flow<Currency>
    val dailyReminderEnabledFlow: Flow<Boolean>
    val reminderTimeFlow: Flow<String>
    val budgetAlertsEnabledFlow: Flow<Boolean>
    val goalRemindersEnabledFlow: Flow<Boolean>

    suspend fun updateBudgetLimit(limit: Double)
    suspend fun updateCurrency(currency: Currency)
    suspend fun updateDailyReminderEnabled(enabled: Boolean)
    suspend fun updateReminderTime(time: String)
    suspend fun updateBudgetAlertsEnabled(enabled: Boolean)
    suspend fun updateGoalRemindersEnabled(enabled: Boolean)
}
