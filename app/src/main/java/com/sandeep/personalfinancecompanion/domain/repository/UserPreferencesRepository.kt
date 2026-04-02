package com.sandeep.personalfinancecompanion.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val budgetLimitFlow: Flow<Double>
    val currencyCodeFlow: Flow<String?>
    suspend fun updateBudgetLimit(limit: Double)
    suspend fun updateCurrencyCode(code: String)
}
