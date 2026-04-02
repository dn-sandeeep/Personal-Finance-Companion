package com.sandeep.personalfinancecompanion.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.usecase.BalanceSummary
import com.sandeep.personalfinancecompanion.domain.usecase.CalculateBalanceUseCase
import com.sandeep.personalfinancecompanion.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.sandeep.personalfinancecompanion.presentation.components.BarEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val balance: BalanceSummary,
        val recentTransactions: List<Transaction>,
        val budgetLimit: Double,
        val totalExpense: Double,
        val weeklyTrend: List<BarEntry>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val calculateBalanceUseCase: CalculateBalanceUseCase,
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Budget limit stored in-memory (in real app this would be persisted)
    private val _budgetLimit = MutableStateFlow(50000.0)
    val budgetLimit: StateFlow<Double> = _budgetLimit.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading


                // Observe transactions reactively
                getTransactionsUseCase().collect { transactions ->
                    val balance = calculateBalanceUseCase(transactions)
                    val weeklyTrend = calculateWeeklyTrend(transactions)
                    
                    _uiState.value = HomeUiState.Success(
                        balance = balance,
                        recentTransactions = transactions.take(5),
                        budgetLimit = _budgetLimit.value,
                        totalExpense = balance.totalExpense,
                        weeklyTrend = weeklyTrend
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    private fun calculateWeeklyTrend(transactions: List<Transaction>): List<BarEntry> {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Initialize map for the last 7 days with 0.0
        val dailyExpenses = mutableMapOf<Int, Double>()
        val days = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, 
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )
        days.forEach { dailyExpenses[it] = 0.0 }

        // Filter transactions for the last 7 days and only expenses
        val oneWeekAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        transactions.filter { 
            it.date >= oneWeekAgo && it.type == com.sandeep.personalfinancecompanion.domain.model.TransactionType.EXPENSE 
        }.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            val day = calendar.get(Calendar.DAY_OF_WEEK)
            dailyExpenses[day] = (dailyExpenses[day] ?: 0.0) + transaction.amount
        }

        val dayLabels = mapOf(
            Calendar.MONDAY to "MON",
            Calendar.TUESDAY to "TUE",
            Calendar.WEDNESDAY to "WED",
            Calendar.THURSDAY to "THU",
            Calendar.FRIDAY to "FRI",
            Calendar.SATURDAY to "SAT",
            Calendar.SUNDAY to "SUN"
        )

        return days.map { day ->
            BarEntry(
                label = dayLabels[day] ?: "",
                value = dailyExpenses[day]?.toFloat() ?: 0f,
                isHighlighted = day == currentDay
            )
        }
    }

    fun updateBudgetLimit(limit: Double) {
        _budgetLimit.value = limit
        // Re-emit state with new limit
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(budgetLimit = limit)
        }
    }

    fun retry() {
        loadDashboard()
    }
}
