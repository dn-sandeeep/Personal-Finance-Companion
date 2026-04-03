package com.sandeep.personalfinancecompanion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import com.sandeep.personalfinancecompanion.util.WorkManagerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val scheduler: WorkManagerScheduler
) : ViewModel() {

    init {
        observeReminders()
    }

    private fun observeReminders() {
        viewModelScope.launch {
            preferencesRepository.dailyReminderEnabledFlow.collectLatest { enabled ->
                if (enabled) {
                    val time = "20:00" // For now daily reminder is 8:00 PM
                    val parts = time.split(":").map { it.toInt() }
                    scheduler.scheduleDailyReminder(parts[0], parts[1])
                } else {
                    scheduler.cancelDailyReminder()
                }
            }
        }

        viewModelScope.launch {
            preferencesRepository.goalRemindersEnabledFlow.collectLatest { enabled ->
                if (enabled) {
                    scheduler.scheduleGoalReminders()
                } else {
                    scheduler.cancelGoalReminders()
                }
            }
        }
    }
}
