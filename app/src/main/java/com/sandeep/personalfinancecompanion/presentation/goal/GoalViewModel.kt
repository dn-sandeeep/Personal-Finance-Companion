package com.sandeep.personalfinancecompanion.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Goal
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.repository.GoalRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.sandeep.personalfinancecompanion.domain.model.NoSpendStreak
import com.sandeep.personalfinancecompanion.domain.model.SavingVelocity
import com.sandeep.personalfinancecompanion.domain.usecase.GetNoSpendStreakUseCase
import com.sandeep.personalfinancecompanion.domain.usecase.GetSavingVelocityUseCase
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface GoalUiState {
    data object Loading : GoalUiState
    data class Success(
        val goals: List<Goal>,
        val currency: Currency,
        val noSpendStreak: NoSpendStreak,
        val savingVelocity: SavingVelocity
    ) : GoalUiState
    data class Error(val message: String) : GoalUiState
}

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val transactionRepository: TransactionRepository,
    private val getNoSpendStreakUseCase: GetNoSpendStreakUseCase,
    private val getSavingVelocityUseCase: GetSavingVelocityUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GoalUiState>(GoalUiState.Loading)
    val uiState: StateFlow<GoalUiState> = combine(
        repository.getAllGoals(),
        preferencesRepository.currencyFlow,
        transactionRepository.getAllTransactions(),
        getNoSpendStreakUseCase()
    ) { goals, currency, transactions, streak ->
        val velocity = getSavingVelocityUseCase(goals, transactions)
        GoalUiState.Success(goals, currency, streak, velocity)
    }.catch { e ->
        _uiState.value = GoalUiState.Error(e.message ?: "An unexpected error occurred")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalUiState.Loading
    )
    
    fun createNewGoal(title: String, targetAmount: Double, iconName: String, colorHex: String, targetDate: Long? = null) {
        viewModelScope.launch {
            try {
                val newGoal = Goal(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    targetAmount = targetAmount,
                    savedAmount = 0.0,
                    iconName = iconName,
                    colorHex = colorHex,
                    contributions = emptyList(),
                    targetDate = targetDate
                )
                repository.insertGoal(newGoal)
            } catch (e: Exception) {
                // Handle error if needed, maybe via a one-time event/SideEffect
            }
        }
    }

    fun updateGoalSettings(goalId: String, targetAmount: Double, targetDate: Long?) {
        viewModelScope.launch {
            repository.updateGoalSettings(goalId, targetAmount, targetDate)
        }
    }

    fun updateGoalPriority(goalId: String, newPriority: Int) {
        viewModelScope.launch {
            repository.updateGoalPriority(goalId, newPriority)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun addSavings(goalId: String, amount: Double) {
        viewModelScope.launch {
            repository.addContribution(goalId, amount)
        }
    }

    fun setNoSpendTarget(days: Int) {
        viewModelScope.launch {
            preferencesRepository.updateNoSpendTarget(days)
        }
    }

    fun retry() {
        // Since we are using stateIn with combine, it automatically retries if the underlying flows emit again
        // but we can also force a refresh if needed.
    }
}
