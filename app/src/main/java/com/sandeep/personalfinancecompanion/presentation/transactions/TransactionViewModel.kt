package com.sandeep.personalfinancecompanion.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import com.sandeep.personalfinancecompanion.domain.usecase.GetTransactionsUseCase
import com.sandeep.personalfinancecompanion.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

data class TransactionListState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val repository: TransactionRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<TransactionType?>(null)
    val selectedFilter: StateFlow<TransactionType?> = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    val listState: StateFlow<TransactionListState> = combine(
        getTransactionsUseCase(),
        _searchQuery,
        _selectedFilter,
        _isLoading
    ) { transactions, query, filter, loading ->
        val filtered = transactions
            .filter { transaction ->
                if (filter != null) transaction.type == filter else true
            }
            .filter { transaction ->
                if (query.isNotBlank()) {
                    transaction.notes.contains(query, ignoreCase = true) ||
                            transaction.category.displayName.contains(query, ignoreCase = true)
                } else true
            }

        TransactionListState(
            transactions = filtered,
            isLoading = loading,
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionListState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: TransactionType?) {
        _selectedFilter.value = filter
    }

    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: Category,
        notes: String,
        date: Long
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                type = type,
                category = category,
                date = date,
                notes = notes
            )
            repository.addTransaction(transaction)
            
            // Check budget if it's an expense
            if (type == TransactionType.EXPENSE) {
                checkBudgetAlert(amount)
            }
        }
    }

    private suspend fun checkBudgetAlert(amount: Double) {
        val enabled = preferencesRepository.budgetAlertsEnabledFlow.first()
        if (!enabled) return

        val budget = preferencesRepository.budgetLimitFlow.first()
        val allTransactions = repository.getAllTransactions().first()
        
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val monthlyExpenses = allTransactions
            .filter { 
                it.type == TransactionType.EXPENSE 
            }
            .filter { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
            }
        
        val totalSpent = monthlyExpenses.sumOf { it.amount }
        val previousSpent = totalSpent - amount
        
        // 100% threshold
        if (previousSpent < budget && totalSpent >= budget) {
            notificationHelper.showBudgetAlert(
                "Budget Exceeded",
                "You have kharch kar diya complete budget ($budget) for this month!"
            )
        } 
        // 80% threshold
        else if (previousSpent < budget * 0.8 && totalSpent >= budget * 0.8) {
            notificationHelper.showBudgetAlert(
                "Budget Alert",
                "You have reached 80% of your monthly budget ($budget)."
            )
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            
            if (transaction.type == TransactionType.EXPENSE) {
                checkBudgetAlert(0.0) // Re-check whole budget
            }
        }
    }

    suspend fun getTransactionById(id: String): Transaction? {
        return repository.getTransactionById(id)
    }
}
