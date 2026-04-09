package com.sandeep.personalfinancecompanion.domain.usecase

import com.sandeep.personalfinancecompanion.domain.repository.GoalRepository
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Use case to fetch all transactions and goals and format them into a CSV string.
 */
class ExportDataUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(): String {
        val transactions = transactionRepository.getAllTransactions().first()
        val goals = goalRepository.getAllGoals().first()
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val csvBuilder = StringBuilder()

        // Transactions Section
        csvBuilder.append("--- TRANSACTIONS ---\n")
        csvBuilder.append("Date,Amount,Type,Category,Notes\n")
        transactions.forEach { tx ->
            val dateStr = dateFormat.format(Date(tx.date))
            val amountStr = tx.amount.toString()
            val typeStr = tx.type.name
            val categoryStr = tx.category.name
            val noteStr = tx.notes.replace(",", ";").replace("\n", " ").trim()
            csvBuilder.append("$dateStr,$amountStr,$typeStr,$categoryStr,$noteStr\n")
        }

        csvBuilder.append("\n")

        // Goals Section
        csvBuilder.append("--- SAVINGS GOALS ---\n")
        csvBuilder.append("Title,Target Amount,Saved Amount,Progress,Target Date\n")
        goals.forEach { goal ->
            val targetDateStr = goal.targetDate?.let { dateFormat.format(Date(it)) } ?: "N/A"
            val progressStr = String.format("%.2f%%", goal.progress * 100)
            csvBuilder.append("${goal.title.replace(",", ";")},${goal.targetAmount},${goal.savedAmount},$progressStr,$targetDateStr\n")
        }

        return csvBuilder.toString()
    }
}
