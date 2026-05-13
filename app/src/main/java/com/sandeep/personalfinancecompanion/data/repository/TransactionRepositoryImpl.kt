package com.sandeep.personalfinancecompanion.data.repository

import com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao
import com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity
import com.sandeep.personalfinancecompanion.data.mapper.toDomain
import com.sandeep.personalfinancecompanion.data.mapper.toEntity
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
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

    override fun getUnsettledUdhaar(): Flow<List<Transaction>> {
        return dao.getUnsettledUdhaar().map { entities ->
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

    override suspend fun addAutoImportedTransaction(transaction: Transaction): Boolean {
        return withContext(Dispatchers.IO) {
            val fingerprint = transaction.sourceFingerprint
            if (!fingerprint.isNullOrBlank() && dao.getTransactionBySourceFingerprint(fingerprint) != null) {
                return@withContext false
            }
            dao.insertTransaction(transaction.toEntity())
            true
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

    override suspend fun convertAllTransactions(factor: Double) {
        withContext(Dispatchers.IO) {
            dao.convertAllTransactions(factor)
        }
    }

    override suspend fun updateSettlementStatus(id: String, isSettled: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateSettlementStatus(id, isSettled)
        }
    }

    override suspend fun cleanupBrokenAutoImportedTransactionsForMay132026(): Int {
        return withContext(Dispatchers.IO) {
            val zoneId = ZoneId.systemDefault()
            val start = LocalDate.of(2026, 5, 13).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val end = LocalDate.of(2026, 5, 14).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val transactions = dao.getTransactionsBetween(start, end)

            val candidateRows = transactions.filter(::isLikelyAutoImported)
            if (candidateRows.isEmpty()) return@withContext 0

            val idsToDelete = linkedSetOf<String>()

            candidateRows
                .filter { !it.sourceFingerprint.isNullOrBlank() }
                .groupBy { it.sourceFingerprint!! }
                .values
                .forEach { duplicates ->
                    duplicates.sortedByDescending(::cleanupScore)
                        .drop(1)
                        .forEach { idsToDelete += it.id }
                }

            candidateRows
                .filter { it.id !in idsToDelete }
                .groupBy(::normalizedSourceKey)
                .filterKeys { it.isNotBlank() }
                .values
                .forEach { group ->
                    if (group.size > 1) {
                        group.sortedByDescending(::cleanupScore)
                            .drop(1)
                            .forEach { idsToDelete += it.id }
                    }
                }

            candidateRows
                .filter { it.id !in idsToDelete }
                .filter(::isObviousAccountDigitAmount)
                .forEach { idsToDelete += it.id }

            if (idsToDelete.isNotEmpty()) {
                dao.deleteTransactionsByIds(idsToDelete.toList())
            }
            idsToDelete.size
        }
    }

    private fun cleanupScore(transaction: TransactionEntity): Int {
        var score = 0
        if (!transaction.sourceFingerprint.isNullOrBlank()) score += 4
        if (!transaction.rawSourceText.isNullOrBlank()) score += 2
        if (matchesAnchoredAmount(transaction.amount, sourceText(transaction))) score += 5
        if (!isObviousAccountDigitAmount(transaction)) score += 3
        return score
    }

    private fun isLikelyAutoImported(transaction: TransactionEntity): Boolean {
        if (!transaction.sourceType.isNullOrBlank()) return true
        val text = sourceText(transaction).lowercase()
        return text.contains("debited") ||
            text.contains("credited") ||
            text.contains("upi") ||
            text.contains("txn") ||
            text.contains("a/c") ||
            text.contains("acct") ||
            text.contains("account") ||
            text.contains("inr") ||
            text.contains("rs")
    }

    private fun normalizedSourceKey(transaction: TransactionEntity): String {
        val source = sourceText(transaction)
        return source.lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun sourceText(transaction: TransactionEntity): String {
        return transaction.rawSourceText
            ?.takeIf { it.isNotBlank() }
            ?: transaction.notes.removePrefix("Detected:").trim()
    }

    private fun matchesAnchoredAmount(amount: Double, text: String): Boolean {
        return extractAnchoredAmounts(text).any { abs(it - amount) < 0.001 }
    }

    private fun isObviousAccountDigitAmount(transaction: TransactionEntity): Boolean {
        val source = sourceText(transaction)
        if (matchesAnchoredAmount(transaction.amount, source)) return false

        val accountDigits = Regex(
            """(?i)(?:a/c|acct|account|card|ending(?:\s+in)?)\s*[:\-]?\s*(?:[*xX]{1,}|\bxx\b|\bxxxx\b)?\s*(\d{3,4})"""
        ).findAll(source).mapNotNull { it.groupValues.getOrNull(1)?.toDoubleOrNull() }.toSet()

        if (accountDigits.isEmpty()) return false
        return accountDigits.any { abs(it - transaction.amount) < 0.001 }
    }

    private fun extractAnchoredAmounts(text: String): List<Double> {
        val patterns = listOf(
            Regex("""(?i)(?:rs\.?|inr|₹)\s*([0-9][\d,]*(?:\.\d{1,2})?)"""),
            Regex("""(?i)(?:amt|amount)\s*(?:of|is|:)?\s*(?:rs\.?|inr|₹)?\s*([0-9][\d,]*(?:\.\d{1,2})?)""")
        )
        return patterns.flatMap { pattern ->
            pattern.findAll(text).mapNotNull { match ->
                match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()
            }.toList()
        }.distinct()
    }
}
