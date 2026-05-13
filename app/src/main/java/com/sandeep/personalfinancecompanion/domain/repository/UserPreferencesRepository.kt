package com.sandeep.personalfinancecompanion.domain.repository

import com.sandeep.personalfinancecompanion.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val budgetLimitFlow: Flow<Double>
    val languageFlow: Flow<String>
    val currencyFlow: Flow<Currency>
    val dailyReminderEnabledFlow: Flow<Boolean>
    val reminderTimeFlow: Flow<String>
    val budgetAlertsEnabledFlow: Flow<Boolean>
    val goalRemindersEnabledFlow: Flow<Boolean>
    val noSpendTargetFlow: Flow<Int>
    val analyticsEnabledFlow: Flow<Boolean>
    val smsDetectionEnabledFlow: Flow<Boolean>
    val autoSaveSmsTransactionsFlow: Flow<Boolean>
    val may132026NotificationCleanupCompletedFlow: Flow<Boolean>

    suspend fun updateBudgetLimit(limit: Double)
    suspend fun updateLanguage(languageCode: String)
    suspend fun updateCurrency(currency: Currency)
    suspend fun updateDailyReminderEnabled(enabled: Boolean)
    suspend fun updateReminderTime(time: String)
    suspend fun updateBudgetAlertsEnabled(enabled: Boolean)
    suspend fun updateGoalRemindersEnabled(enabled: Boolean)
    suspend fun updateNoSpendTarget(days: Int)
    suspend fun updateAnalyticsEnabled(enabled: Boolean)
    suspend fun updateSmsDetectionEnabled(enabled: Boolean)
    suspend fun updateAutoSaveSmsTransactions(enabled: Boolean)
    suspend fun updateMay132026NotificationCleanupCompleted(completed: Boolean)
}
