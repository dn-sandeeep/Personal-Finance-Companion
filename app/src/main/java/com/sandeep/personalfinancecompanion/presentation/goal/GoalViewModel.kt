package com.sandeep.personalfinancecompanion.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Goal
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.repository.GoalRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.sandeep.personalfinancecompanion.domain.model.NoSpendStreak
import com.sandeep.personalfinancecompanion.domain.usecase.GetNoSpendStreakUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val getNoSpendStreakUseCase: GetNoSpendStreakUseCase
) : ViewModel() {
    
    val goals: StateFlow<List<Goal>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val currency: StateFlow<Currency> = preferencesRepository.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.INR)

    val noSpendStreak: StateFlow<NoSpendStreak> = getNoSpendStreakUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoSpendStreak(0, "Calculating...", true))
    
    fun createNewGoal(title: String, targetAmount: Double, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val newGoal = Goal(
                id = UUID.randomUUID().toString(),
                title = title,
                targetAmount = targetAmount,
                savedAmount = 0.0,
                iconName = iconName,
                colorHex = colorHex,
                contributions = emptyList()
            )
            repository.insertGoal(newGoal)
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
}
