package com.sandeep.personalfinancecompanion.data.repository

import com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao
import com.sandeep.personalfinancecompanion.data.mapper.toDomain
import com.sandeep.personalfinancecompanion.data.mapper.toEntity
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return withContext(Dispatchers.IO) {
            dao.getTransactionById(id)?.toDomain()
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            dao.insertTransaction(transaction.toEntity())
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            dao.updateTransaction(transaction.toEntity())
        }
    }

    override suspend fun deleteTransaction(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteTransaction(id)
        }
    }
}
