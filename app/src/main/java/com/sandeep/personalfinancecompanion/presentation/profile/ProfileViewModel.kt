package com.sandeep.personalfinancecompanion.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.analytics.AnalyticsEvent
import com.sandeep.personalfinancecompanion.analytics.AnalyticsParam
import com.sandeep.personalfinancecompanion.analytics.AnalyticsTracker
import com.sandeep.personalfinancecompanion.analytics.CrashReporter
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import com.sandeep.personalfinancecompanion.domain.usecase.ExportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    val selectedLanguage: String = "en",
    val analyticsEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val exportStatus: ExportStatus = ExportStatus.Idle
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val changeCurrencyUseCase: com.sandeep.personalfinancecompanion.domain.usecase.ChangeCurrencyUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter
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
    }.combine(preferencesRepository.analyticsEnabledFlow) { currentState, analyticsEnabled ->
        currentState.copy(analyticsEnabled = analyticsEnabled)
    }.combine(preferencesRepository.languageFlow) { currentState, language ->
        currentState.copy(selectedLanguage = language)
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
            analyticsTracker.trackEvent(
                AnalyticsEvent.CURRENCY_CHANGED,
                mapOf(AnalyticsParam.CURRENCY to currency.code)
            )
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.updateLanguage(languageCode)
            analyticsTracker.trackEvent(
                AnalyticsEvent.LANGUAGE_CHANGED,
                mapOf(AnalyticsParam.LANGUAGE to languageCode)
            )
        }
    }

    fun updateAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) {
                analyticsTracker.trackEvent(
                    AnalyticsEvent.ANALYTICS_CONSENT_CHANGED,
                    mapOf(AnalyticsParam.ENABLED to false)
                )
            }
            preferencesRepository.updateAnalyticsEnabled(enabled)
            analyticsTracker.setCollectionEnabled(enabled)
            crashReporter.setCollectionEnabled(enabled)
            crashReporter.setUserProperty("telemetry_opt_in", enabled.toString())
            if (enabled) {
                analyticsTracker.trackEvent(
                    AnalyticsEvent.ANALYTICS_CONSENT_CHANGED,
                    mapOf(AnalyticsParam.ENABLED to true)
                )
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Loading
            analyticsTracker.trackEvent(AnalyticsEvent.EXPORT_STARTED)
            try {
                val csvData = exportDataUseCase()
                _exportStatus.value = ExportStatus.Success(csvData)
                analyticsTracker.trackEvent(AnalyticsEvent.EXPORT_COMPLETED)
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Unknown error occurred")
                analyticsTracker.trackEvent(
                    AnalyticsEvent.EXPORT_FAILED,
                    mapOf(AnalyticsParam.RESULT to "error")
                )
            }
        }
    }

    fun resetExportStatus() {
        _exportStatus.value = ExportStatus.Idle
    }
}
