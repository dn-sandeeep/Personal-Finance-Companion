package com.sandeep.personalfinancecompanion.domain.usecase

import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import javax.inject.Inject

data class BalanceSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val currentBalance: Double
)

class CalculateBalanceUseCase @Inject constructor() {

    operator fun invoke(transactions: List<Transaction>): BalanceSummary {
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }

        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val totalBorrowed = transactions
            .filter { it.type == TransactionType.BORROWED }
            .sumOf { it.amount }

        val totalLent = transactions
            .filter { it.type == TransactionType.LENT }
            .sumOf { it.amount }

        return BalanceSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            currentBalance = (totalIncome + totalBorrowed) - (totalExpense + totalLent)
        )
    }
}
