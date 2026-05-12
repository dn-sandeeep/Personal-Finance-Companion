package com.sandeep.personalfinancecompanion.domain.parser

import android.content.Context
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
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

    private val incomeKeywords = listOf("received", "income", "earned", "salary", "bonus", "get", "got", "credit", "credited", "aaye", "mile", "jama", "pagaar", "stipend", "added", "cashback", "refund", "deposit")
    private val expenseKeywords = listOf("spent", "paid", "bought", "kharch", "debit", "debited", "diye", "nikale", "dia", "bhara", "gave", "lowered", "withdrawn", "purchase")

    /**
     * Normalizes Unicode text by converting Mathematical Alphanumeric Symbols
     * (used by RCS messages from banks like PNB) to standard ASCII characters.
     * e.g., 𝖽𝖾𝖻𝗂𝗍𝖾𝖽 → debited, 𝖨𝖭𝖱 → INR
     */
    private fun normalizeUnicode(text: String): String {
        val sb = StringBuilder()
        for (cp in text.codePoints().toArray()) {
            val normalized = when (cp) {
                // Mathematical Sans-Serif Bold Small (U+1D5EE - U+1D607) → a-z
                in 0x1D5EE..0x1D607 -> ('a' + (cp - 0x1D5EE))
                // Mathematical Sans-Serif Bold Capital (U+1D5D4 - U+1D5ED) → A-Z
                in 0x1D5D4..0x1D5ED -> ('A' + (cp - 0x1D5D4))
                // Mathematical Bold Small (U+1D41A - U+1D433) → a-z
                in 0x1D41A..0x1D433 -> ('a' + (cp - 0x1D41A))
                // Mathematical Bold Capital (U+1D400 - U+1D419) → A-Z
                in 0x1D400..0x1D419 -> ('A' + (cp - 0x1D400))
                // Mathematical Sans-Serif Small (U+1D5BA - U+1D5D3) → a-z
                in 0x1D5BA..0x1D5D3 -> ('a' + (cp - 0x1D5BA))
                // Mathematical Sans-Serif Capital (U+1D5A0 - U+1D5B9) → A-Z
                in 0x1D5A0..0x1D5B9 -> ('A' + (cp - 0x1D5A0))
                // Mathematical Italic Small (U+1D44E - U+1D467) → a-z
                in 0x1D44E..0x1D467 -> ('a' + (cp - 0x1D44E))
                // Mathematical Italic Capital (U+1D434 - U+1D44D) → A-Z
                in 0x1D434..0x1D44D -> ('A' + (cp - 0x1D434))
                // Mathematical Bold Italic Small (U+1D482 - U+1D49B) → a-z
                in 0x1D482..0x1D49B -> ('a' + (cp - 0x1D482))
                // Mathematical Bold Italic Capital (U+1D468 - U+1D481) → A-Z
                in 0x1D468..0x1D481 -> ('A' + (cp - 0x1D468))
                // Mathematical Monospace Small (U+1D68A - U+1D6A3) → a-z
                in 0x1D68A..0x1D6A3 -> ('a' + (cp - 0x1D68A))
                // Mathematical Monospace Capital (U+1D670 - U+1D689) → A-Z
                in 0x1D670..0x1D689 -> ('A' + (cp - 0x1D670))
                // Mathematical Bold Digits (U+1D7CE - U+1D7D7) → 0-9
                in 0x1D7CE..0x1D7D7 -> ('0' + (cp - 0x1D7CE))
                // Mathematical Sans-Serif Bold Digits (U+1D7EC - U+1D7F5) → 0-9
                in 0x1D7EC..0x1D7F5 -> ('0' + (cp - 0x1D7EC))
                // Mathematical Monospace Digits (U+1D7F6 - U+1D7FF) → 0-9
                in 0x1D7F6..0x1D7FF -> ('0' + (cp - 0x1D7F6))
                // Fullwidth digits (U+FF10 - U+FF19) → 0-9
                in 0xFF10..0xFF19 -> ('0' + (cp - 0xFF10))
                // Fullwidth Latin Capital (U+FF21 - U+FF3A) → A-Z
                in 0xFF21..0xFF3A -> ('A' + (cp - 0xFF21))
                // Fullwidth Latin Small (U+FF41 - U+FF5A) → a-z
                in 0xFF41..0xFF5A -> ('a' + (cp - 0xFF41))
                else -> null
            }
            if (normalized != null) {
                sb.append(normalized)
            } else {
                sb.appendCodePoint(cp)
            }
        }
        return sb.toString()
    }

    suspend fun parse(context: Context, text: String): List<ParsedTransaction> {
        // Normalize Unicode (RCS messages from banks use fancy Unicode characters)
        val normalizedText = normalizeUnicode(text)
        val extractor = EntityExtraction.getClient(
            EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH).build()
        )

        val results = mutableListOf<ParsedTransaction>()
        try {
            extractor.downloadModelIfNeeded().await()
            val annotations = extractor.annotate(normalizedText).await()

            if (annotations.isEmpty()) {
                // Fallback to manual extraction if ML Kit finds nothing
                val amounts = extractAllNumbers(normalizedText)
                if (amounts.isEmpty()) return emptyList()

                for (amount in amounts) {
                    results.add(
                        ParsedTransaction(
                            amount = amount,
                            category = detectCategory(normalizedText),
                            type = detectType(normalizedText),
                            notes = normalizedText
                        )
                    )
                }
                return results
            }

            for (annotation in annotations) {
                for (entity in annotation.entities) {
                    if (entity.type == Entity.TYPE_MONEY) {
                        val moneyText = normalizedText.substring(annotation.start, annotation.end)
                        val amount = extractNumber(moneyText) ?: continue
                        
                        // Look at surrounding context (40 chars before/after) to find category
                        // A wider window helps identify Income vs Expense more specifically
                        val start = (annotation.start - 40).coerceAtLeast(0)
                        val end = (annotation.end + 40).coerceAtMost(normalizedText.length)
                        val contextText = normalizedText.substring(start, end).lowercase()
                        
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
                extractAllNumbers(normalizedText).forEach { amount ->
                   results.add(ParsedTransaction(amount = amount, category = detectCategory(normalizedText), type = detectType(normalizedText), notes = normalizedText))
                }
            }

        } catch (e: Exception) {
            extractAllNumbers(normalizedText).forEach { amount ->
                results.add(ParsedTransaction(amount = amount, category = detectCategory(normalizedText), type = detectType(normalizedText), notes = normalizedText))
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
        // Bank specific debit/credit detection
        if (lowerText.contains("debited") || lowerText.contains("withdrawn") || lowerText.contains("spent at")) return TransactionType.EXPENSE
        if (lowerText.contains("credited") || lowerText.contains("received") || lowerText.contains("deposit")) return TransactionType.INCOME

        // General keywords
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
