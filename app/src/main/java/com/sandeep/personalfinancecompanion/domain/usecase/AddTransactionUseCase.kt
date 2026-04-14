package com.sandeep.personalfinancecompanion.domain.usecase

import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import java.util.UUID
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        amount: Double,
        category: Category,
        type: TransactionType,
        notes: String = ""
    ): Result<Unit> {
        return try {
            if (amount <= 0) {
                return Result.failure(Exception("Amount must be greater than zero"))
            }

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                type = type,
                category = category,
                date = System.currentTimeMillis(),
                notes = notes
            )
            repository.addTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
