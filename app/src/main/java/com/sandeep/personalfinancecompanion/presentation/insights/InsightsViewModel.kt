package com.sandeep.personalfinancecompanion.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.usecase.BalanceSummary
import com.sandeep.personalfinancecompanion.domain.usecase.CalculateBalanceUseCase
import com.sandeep.personalfinancecompanion.domain.usecase.GetTransactionsUseCase
import com.sandeep.personalfinancecompanion.presentation.components.PieChartEntry
import com.sandeep.personalfinancecompanion.ui.theme.ChartColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsState(
    val isLoading: Boolean = true,
    val categoryBreakdown: List<PieChartEntry> = emptyList(),
    val balanceSummary: BalanceSummary = BalanceSummary(0.0, 0.0, 0.0),
    val topCategory: String = "",
    val totalTransactions: Int = 0,
    val error: String? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val calculateBalanceUseCase: CalculateBalanceUseCase,
    private val repository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            try {

                getTransactionsUseCase().collect { transactions ->
                    val balance = calculateBalanceUseCase(transactions)

                    // Category breakdown for expenses only
                    val expensesByCategory = transactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                        .toList()
                        .sortedByDescending { it.second }

                    val chartEntries = expensesByCategory.mapIndexed { index, (category, amount) ->
                        PieChartEntry(
                            label = "${category.emoji} ${category.displayName}",
                            value = amount,
                            color = ChartColors[index % ChartColors.size]
                        )
                    }

                    val topCategory = if (expensesByCategory.isNotEmpty()) {
                        val top = expensesByCategory.first()
                        "${top.first.emoji} ${top.first.displayName}"
                    } else "N/A"

                    _state.value = InsightsState(
                        isLoading = false,
                        categoryBreakdown = chartEntries,
                        balanceSummary = balance,
                        topCategory = topCategory,
                        totalTransactions = transactions.size,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = InsightsState(
                    isLoading = false,
                    error = e.message ?: "Failed to load insights"
                )
            }
        }
    }
}
