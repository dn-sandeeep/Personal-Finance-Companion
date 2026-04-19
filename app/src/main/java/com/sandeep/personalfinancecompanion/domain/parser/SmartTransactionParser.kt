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
        Category.FOOD to listOf("pizza", "burger", "lunch", "dinner", "tea", "chai", "swiggy", "zomato", "ice cream", "restaurant", "coffee", "starbucks", "khana", "nashta"),
        Category.TRANSPORT to listOf("petrol", "diesel", "rickshaw", "uber", "ola", "metro", "fuel", "bus", "train", "ticket", "auto", "cab"),
        Category.SHOPPING to listOf("clothes", "amazon", "flipkart", "mall", "laptop", "mobile", "shoes", "dress", "kharidari"),
        Category.ENTERTAINMENT to listOf("movie", "netflix", "game", "concert", "theater", "spotify", "cinema", "club"),
        Category.BILLS to listOf("rent", "electricity", "water", "internet", "recharge", "fastag", "insurance", "bill", "kiraya"),
        Category.HEALTH to listOf("doctor", "medicine", "hospital", "gym", "pharmacy", "dawai"),
        Category.EDUCATION to listOf("fees", "book", "course", "school", "college"),
        Category.SALARY to listOf("salary", "bonus", "stipend", "earnings", "pagaar"),
        Category.INVESTMENT to listOf("stock", "mutual fund", "crypto", "gold", "sip"),
        Category.GIFT to listOf("gift", "shagun", "birthday", "tohfa")
    )

    private val incomeKeywords = listOf("received", "earned", "salary", "bonus", "get", "credit", "aaye", "mile")
    private val expenseKeywords = listOf("spent", "paid", "bought", "spent", "debit", "diye", "kharch")

    suspend fun parse(context: Context, text: String): ParsedTransaction {
        val extractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
        )

        return try {
            extractor.downloadModelIfNeeded().await()
            val entities = extractor.annotate(text).await()

            var amount: Double? = null
            var date = Date()

            for (annotation in entities) {
                for (entity in annotation.entities) {
                    when (entity.type) {
                        Entity.TYPE_MONEY -> {
                            // Extract numerical value from money entity if possible
                            // Note: ML Kit might return it as a string or special object
                            // For simplicity, we can also try parsing the text within the annotation
                            val moneyText = text.substring(annotation.start, annotation.end)
                            amount = extractNumber(moneyText)
                        }
                        Entity.TYPE_DATE_TIME -> {
                            // Can extract date if needed
                        }
                    }
                }
            }

            val category = detectCategory(text)
            val type = detectType(text)

            ParsedTransaction(
                amount = amount ?: extractNumber(text), // Fallback to manual extract if ML Kit fails
                category = category,
                type = type,
                date = date,
                notes = text
            )
        } catch (e: Exception) {
            // Fallback to basic parsing if ML Kit fails
            ParsedTransaction(
                amount = extractNumber(text),
                category = detectCategory(text),
                type = detectType(text),
                notes = text
            )
        } finally {
            extractor.close()
        }
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
        if (incomeKeywords.any { lowerText.contains(it) }) return TransactionType.INCOME
        if (expenseKeywords.any { lowerText.contains(it) }) return TransactionType.EXPENSE
        return TransactionType.EXPENSE // Default
    }

    private fun extractNumber(text: String): Double? {
        val regex = Regex("""\d+(\.\d+)?""")
        return regex.find(text)?.value?.toDoubleOrNull()
    }
}
