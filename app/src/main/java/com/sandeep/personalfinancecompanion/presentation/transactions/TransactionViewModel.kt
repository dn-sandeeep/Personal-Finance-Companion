package com.sandeep.personalfinancecompanion.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val repository: TransactionRepository
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
        }
    }
}
