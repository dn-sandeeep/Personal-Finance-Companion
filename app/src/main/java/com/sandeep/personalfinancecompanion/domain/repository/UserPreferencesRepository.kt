package com.sandeep.personalfinancecompanion.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val budgetLimitFlow: Flow<Double>
    suspend fun updateBudgetLimit(limit: Double)
}
