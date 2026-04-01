package com.sandeep.personalfinancecompanion.data.repository

import com.sandeep.personalfinancecompanion.data.mapper.toDomain
import com.sandeep.personalfinancecompanion.data.mapper.toDto
import com.sandeep.personalfinancecompanion.data.remote.TransactionApiService
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val apiService: TransactionApiService
) : TransactionRepository {

    // Local cache that acts as our single source of truth for the UI
    private val _cachedTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    private var isInitialized = false

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return _cachedTransactions.map { transactions ->
            transactions.sortedByDescending { it.date }
        }
    }

    suspend fun refreshTransactions() {
        val dtos = apiService.getAllTransactions()
        _cachedTransactions.value = dtos.map { it.toDomain() }
        isInitialized = true
    }

    suspend fun ensureInitialized() {
        if (!isInitialized) {
            refreshTransactions()
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        ensureInitialized()
        return _cachedTransactions.value.find { it.id == id }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        val dto = transaction.toDto()
        apiService.addTransaction(dto)
        refreshTransactions()
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val dto = transaction.toDto()
        apiService.updateTransaction(dto)
        refreshTransactions()
    }

    override suspend fun deleteTransaction(id: String) {
        apiService.deleteTransaction(id)
        refreshTransactions()
    }
}
