package com.sandeep.personalfinancecompanion.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    }

    override val budgetLimitFlow: Flow<Double> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BUDGET_LIMIT] ?: 50000.0
        }

    override val currencyCodeFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENCY_CODE]
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
}
