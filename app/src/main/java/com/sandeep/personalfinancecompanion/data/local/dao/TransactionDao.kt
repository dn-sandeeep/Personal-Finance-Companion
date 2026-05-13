package com.sandeep.personalfinancecompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSettled = 0 AND (type = 'BORROWED' OR type = 'LENT' OR type = 'BORROWED_REPAYMENT' OR type = 'LENT_REPAYMENT') ORDER BY date DESC")
    fun getUnsettledUdhaar(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :sinceDate ORDER BY date DESC")
    fun getTransactionsSince(sinceDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date < :endDate ORDER BY date DESC")
    suspend fun getTransactionsBetween(startDate: Long, endDate: Long): List<TransactionEntity>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE sourceFingerprint = :fingerprint LIMIT 1")
    fun getTransactionBySourceFingerprint(fingerprint: String): TransactionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: TransactionEntity)
    
    @Update
    fun updateTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    fun deleteTransaction(id: String)

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteTransactionsByIds(ids: List<String>)

    @Query("UPDATE transactions SET isSettled = :isSettled WHERE id = :id")
    suspend fun updateSettlementStatus(id: String, isSettled: Boolean)

    @Query("UPDATE transactions SET amount = amount * :factor")
    suspend fun convertAllTransactions(factor: Double)
}
