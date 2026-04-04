package com.sandeep.personalfinancecompanion.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val dailyReminderEnabled: Boolean = false,
    val reminderTime: String = "20:00",
    val budgetAlertsEnabled: Boolean = true,
    val goalRemindersEnabled: Boolean = true,
    val selectedCurrency: Currency = Currency.INR,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val changeCurrencyUseCase: com.sandeep.personalfinancecompanion.domain.usecase.ChangeCurrencyUseCase
) : ViewModel() {

    val state: StateFlow<ProfileState> = combine(
        preferencesRepository.dailyReminderEnabledFlow,
        preferencesRepository.reminderTimeFlow,
        preferencesRepository.budgetAlertsEnabledFlow,
        preferencesRepository.goalRemindersEnabledFlow,
        preferencesRepository.currencyFlow
    ) { daily, time, budget, goals, currency ->
        ProfileState(
            dailyReminderEnabled = daily,
            reminderTime = time,
            budgetAlertsEnabled = budget,
            goalRemindersEnabled = goals,
            selectedCurrency = currency,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileState()
    )

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDailyReminderEnabled(enabled)
        }
    }

    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            preferencesRepository.updateReminderTime(time)
        }
    }

    fun toggleBudgetAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateBudgetAlertsEnabled(enabled)
        }
    }

    fun toggleGoalReminders(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateGoalRemindersEnabled(enabled)
        }
    }

    fun updateCurrency(currency: Currency) {
        viewModelScope.launch {
            changeCurrencyUseCase(currency)
        }
    }
}
