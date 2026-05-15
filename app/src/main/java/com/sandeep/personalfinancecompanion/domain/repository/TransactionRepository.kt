package com.sandeep.personalfinancecompanion.domain.repository

import com.sandeep.personalfinancecompanion.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getUnsettledUdhaar(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    suspend fun addTransaction(transaction: Transaction)
    suspend fun addAutoImportedTransaction(transaction: Transaction): Boolean
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    suspend fun convertAllTransactions(factor: Double)
    suspend fun updateSettlementStatus(id: String, isSettled: Boolean)
    suspend fun cleanupBrokenAutoImportedTransactionsForMay132026(): Int
    suspend fun cleanupMalformedPnbNotificationCandidatesForMay152026(): Int
}
