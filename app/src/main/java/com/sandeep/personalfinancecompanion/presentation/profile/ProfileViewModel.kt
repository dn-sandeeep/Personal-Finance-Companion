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
import com.sandeep.personalfinancecompanion.domain.usecase.ExportDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed class ExportStatus {
    object Idle : ExportStatus()
    object Loading : ExportStatus()
    data class Success(val data: String) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}

data class ProfileState(
    val dailyReminderEnabled: Boolean = false,
    val reminderTime: String = "20:00",
    val budgetAlertsEnabled: Boolean = true,
    val goalRemindersEnabled: Boolean = true,
    val selectedCurrency: Currency = Currency.INR,
    val isLoading: Boolean = true,
    val exportStatus: ExportStatus = ExportStatus.Idle
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val changeCurrencyUseCase: com.sandeep.personalfinancecompanion.domain.usecase.ChangeCurrencyUseCase,
    private val exportDataUseCase: ExportDataUseCase
) : ViewModel() {

    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)

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
    }.combine(_exportStatus) { currentState, export ->
        currentState.copy(exportStatus = export)
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

    fun exportData() {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Loading
            try {
                val csvData = exportDataUseCase()
                _exportStatus.value = ExportStatus.Success(csvData)
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetExportStatus() {
        _exportStatus.value = ExportStatus.Idle
    }
}
