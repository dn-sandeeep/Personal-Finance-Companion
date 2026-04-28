package com.sandeep.personalfinancecompanion.domain.parser

import android.content.Context
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartTransactionParser @Inject constructor() {

    private val categoryKeywords = mapOf(
        Category.FOOD to listOf("pizza", "burger", "lunch", "dinner", "tea", "chai", "swiggy", "zomato", "ice cream", "restaurant", "coffee", "starbucks", "khana", "khane", "nashta", "nashte", "biscuit", "doodh", "milk", "egg", "ande", "hotel"),
        Category.TRANSPORT to listOf("petrol", "diesel", "rickshaw", "uber", "ola", "metro", "fuel", "bus", "train", "ticket", "auto", "cab", "parking", "puncture", "service", "repair", "toll", "fastag"),
        Category.SHOPPING to listOf("clothes", "kapde", "amazon", "flipkart", "mall", "laptop", "mobile", "shoes", "dress", "kharidari", "grocery", "sabzi", "mandi", "kirana", "mart"),
        Category.ENTERTAINMENT to listOf("movie", "netflix", "game", "concert", "theater", "spotify", "cinema", "club", "outing", "trip", "party"),
        Category.BILLS to listOf("rent", "electricity", "water", "internet", "recharge", "fastag", "insurance", "bill", "kiraya", "bijli", "wifi", "broadband", "mobile bill"),
        Category.HEALTH to listOf("doctor", "medicine", "hospital", "gym", "pharmacy", "dawai", "clinic", "checkup", "tablet", "ayurvedic"),
        Category.EDUCATION to listOf("fees", "book", "course", "school", "college", "tutor", "exam", "notebook", "pen", "coaching"),
        Category.SALARY to listOf("salary", "bonus", "stipend", "earnings", "pagaar", "tankha", "income"),
        Category.INVESTMENT to listOf("stock", "mutual fund", "crypto", "gold", "sip", "fd", "lic", "policy", "shares"),
        Category.GIFT to listOf("gift", "shagun", "birthday", "tohfa", "anniversary", "present")
    )

    private val incomeKeywords = listOf("received", "income", "earned", "salary", "bonus", "get", "got", "credit", "aaye", "mile", "jama", "pagaar", "stipend", "added", "cashback", "refund")
    private val expenseKeywords = listOf("spent", "paid", "bought", "kharch", "debit", "diye", "nikale", "dia", "bhara", "gave", "lowered")

    suspend fun parse(context: Context, text: String): List<ParsedTransaction> {
        val extractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
        )

        val results = mutableListOf<ParsedTransaction>()
        try {
            extractor.downloadModelIfNeeded().await()
            val annotations = extractor.annotate(text).await()

            if (annotations.isEmpty()) {
                // Fallback to manual extraction if ML Kit finds nothing
                val amounts = extractAllNumbers(text)
                if (amounts.isEmpty()) return emptyList()

                for (amount in amounts) {
                    results.add(
                        ParsedTransaction(
                            amount = amount,
                            category = detectCategory(text),
                            type = detectType(text),
                            notes = text
                        )
                    )
                }
                return results
            }

            for (annotation in annotations) {
                for (entity in annotation.entities) {
                    if (entity.type == Entity.TYPE_MONEY) {
                        val moneyText = text.substring(annotation.start, annotation.end)
                        val amount = extractNumber(moneyText) ?: continue
                        
                        // Look at surrounding context (40 chars before/after) to find category
                        // A wider window helps identify Income vs Expense more specifically
                        val start = (annotation.start - 40).coerceAtLeast(0)
                        val end = (annotation.end + 40).coerceAtMost(text.length)
                        val contextText = text.substring(start, end).lowercase()
                        
                        results.add(
                            ParsedTransaction(
                                amount = amount,
                                category = detectCategory(contextText),
                                type = detectType(contextText),
                                notes = "Detected: $moneyText"
                            )
                        )
                    }
                }
            }
            
            if (results.isEmpty()) {
                extractAllNumbers(text).forEach { amount ->
                   results.add(ParsedTransaction(amount = amount, category = detectCategory(text), type = detectType(text), notes = text))
                }
            }

        } catch (e: Exception) {
            extractAllNumbers(text).forEach { amount ->
                results.add(ParsedTransaction(amount = amount, category = detectCategory(text), type = detectType(text), notes = text))
            }
        } finally {
            extractor.close()
        }
        return results
    }

    private fun detectCategory(text: String): Category {
        val lowerText = text.lowercase()
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { lowerText.contains(it) }) {
                return category
            }
        }
        return Category.OTHER
    }

    private fun detectType(text: String): TransactionType {
        val lowerText = text.lowercase()
        // Check for specific income keywords near this amount
        if (incomeKeywords.any { lowerText.contains(it) }) return TransactionType.INCOME
        if (expenseKeywords.any { lowerText.contains(it) }) return TransactionType.EXPENSE
        return TransactionType.EXPENSE // Default
    }

    private fun extractNumber(text: String): Double? {
        val regex = Regex("""\d+(\.\d+)?""")
        return regex.find(text)?.value?.toDoubleOrNull()
    }

    private fun extractAllNumbers(text: String): List<Double> {
        val regex = Regex("""\d+(\.\d+)?""")
        return regex.findAll(text).mapNotNull { it.value.toDoubleOrNull() }.toList()
    }
}
