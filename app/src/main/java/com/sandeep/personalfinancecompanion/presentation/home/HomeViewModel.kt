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

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val balance: BalanceSummary,
        val recentTransactions: List<Transaction>,
        val budgetLimit: Double,
        val totalExpense: Double
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

                repository.ensureInitialized()

                // Observe transactions reactively
                getTransactionsUseCase().collect { transactions ->
                    val balance = calculateBalanceUseCase(transactions)
                    _uiState.value = HomeUiState.Success(
                        balance = balance,
                        recentTransactions = transactions.take(5),
                        budgetLimit = _budgetLimit.value,
                        totalExpense = balance.totalExpense
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = e.message ?: "An unexpected error occurred"
                )
            }
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
