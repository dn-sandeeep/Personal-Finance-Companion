package com.sandeep.personalfinancecompanion.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
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
import com.sandeep.personalfinancecompanion.domain.model.Category

data class CategoryStats(
    val category: Category,
    val amount: Double,
    val transactionCount: Int,
    val percentage: Float
)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val balance: BalanceSummary,
        val recentTransactions: List<Transaction>,
        val budgetLimit: Double,
        val totalExpense: Double,
        val weeklyTrend: List<BarEntry>,
        val categoryExpenses: List<CategoryStats> = emptyList(),
        val selectedDayTransactions: List<Transaction>? = null,
        val selectedDayLabel: String? = null,
        val selectedCategoryTransactions: List<Transaction>? = null,
        val selectedCategoryLabel: String? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val calculateBalanceUseCase: CalculateBalanceUseCase,
    private val repository: TransactionRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()
    private var currentBudgetLimit: Double = 50000.0

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading

                // Collect preferences first to get budget
                launch {
                    preferencesRepository.budgetLimitFlow.collect { limit ->
                        currentBudgetLimit = limit
                        val currentState = _uiState.value
                        if (currentState is HomeUiState.Success) {
                            _uiState.value = currentState.copy(budgetLimit = limit)
                        }
                    }
                }

                // Observe transactions reactively
                getTransactionsUseCase().collect { transactions ->
                    allTransactions = transactions
                    val balance = calculateBalanceUseCase(transactions)
                    val weeklyTrend = calculateWeeklyTrend(transactions)
                    
                    val thirtyDaysAgo = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -30)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis

                    val last30DaysExpenses = transactions.filter {
                        it.date >= thirtyDaysAgo && it.type == com.sandeep.personalfinancecompanion.domain.model.TransactionType.EXPENSE
                    }
                    val total30DaysExpense = last30DaysExpenses.sumOf { it.amount }.toFloat()
                    val categoryStatsList = last30DaysExpenses
                        .groupBy { it.category }
                        .map { (cat, txs) ->
                            val amount = txs.sumOf { it.amount }
                            CategoryStats(
                                category = cat,
                                amount = amount,
                                transactionCount = txs.size,
                                percentage = if (total30DaysExpense > 0f) (amount.toFloat() / total30DaysExpense) else 0f
                            )
                        }
                        .sortedByDescending { it.amount }

                    _uiState.value = HomeUiState.Success(
                        balance = balance,
                        recentTransactions = transactions.take(5),
                        budgetLimit = currentBudgetLimit,
                        totalExpense = balance.totalExpense,
                        weeklyTrend = weeklyTrend,
                        categoryExpenses = categoryStatsList
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
                isHighlighted = day == currentDay,
                dayOfWeek = day
            )
        }
    }

    fun selectDay(dayOfWeek: Int) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val calendar = Calendar.getInstance()
            
            // Filter transactions for that specific day of week within last 7 days
            val oneWeekAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val filteredTransactions = allTransactions.filter { 
                calendar.timeInMillis = it.date
                it.date >= oneWeekAgo && calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek
            }.sortedByDescending { it.date }

            val dayName = when(dayOfWeek) {
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                Calendar.SATURDAY -> "Saturday"
                Calendar.SUNDAY -> "Sunday"
                else -> ""
            }

            _uiState.value = currentState.copy(
                selectedDayTransactions = filteredTransactions,
                selectedDayLabel = dayName
            )
        }
    }

    fun clearSelectedDay() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(
                selectedDayTransactions = null,
                selectedDayLabel = null,
                selectedCategoryTransactions = null,
                selectedCategoryLabel = null
            )
        }
    }

    fun selectCategory(category: Category) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val thirtyDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val filteredTransactions = allTransactions.filter {
                it.category == category && it.date >= thirtyDaysAgo && it.type == com.sandeep.personalfinancecompanion.domain.model.TransactionType.EXPENSE
            }.sortedByDescending { it.date }

            _uiState.value = currentState.copy(
                selectedCategoryTransactions = filteredTransactions,
                selectedCategoryLabel = category.displayName
            )
        }
    }

    fun updateBudgetLimit(limit: Double) {
        viewModelScope.launch {
            preferencesRepository.updateBudgetLimit(limit)
        }
    }

    fun retry() {
        loadDashboard()
    }
}
