package com.sandeep.personalfinancecompanion.util

import java.text.Normalizer

object NotificationTransactionFilter {
    private val promotionalKeywords = listOf(
        "advertisement",
        "ad ",
        "offer",
        "sale",
        "discount",
        "coupon",
        "voucher",
        "cashback offer",
        "reward",
        "rewards",
        "win",
        "deal",
        "deals",
        "save",
        "upto",
        "up to",
        "flat",
        "% off",
        "off on",
        "shop now",
        "limited period",
        "limited time"
    )

    private val strongTransactionKeywords = listOf(
        "debited",
        "credited",
        "withdrawn",
        "spent",
        "paid",
        "payment",
        "received",
        "sent",
        "deposited",
        "completed",
        "successful",
        "purchase",
        "purchased",
        "txn",
        "transaction",
        "upi",
        "vpa",
        "neft",
        "imps",
        "rtgs"
    )

    private val amountPattern = Regex("""(?i)(rs\.?|inr|₹)\s*[\d,]+(?:\.\d{1,2})?""")
    private val accountPattern = Regex("""(?i)(a/c|acct|account|card|ending(?:\s+in)?)\s*[:\-]?\s*(?:[*xX]{1,}|\bxx\b|\bxxxx\b)?\s*\d{3,4}""")

    data class Decision(
        val shouldProcess: Boolean,
        val reason: String
    )

    fun shouldProcess(
        packageName: String,
        title: String,
        text: String,
        extraTexts: List<String> = emptyList()
    ): Boolean {
        return evaluate(packageName, title, text, extraTexts).shouldProcess
    }

    fun evaluate(
        packageName: String,
        title: String,
        text: String,
        extraTexts: List<String> = emptyList()
    ): Decision {
        val combinedText = buildList {
            add(title.trim())
            add(text.trim())
            extraTexts.forEach { add(it.trim()) }
        }
            .filter { it.isNotBlank() }
            .joinToString(" ")
        if (combinedText.isBlank()) {
            return Decision(false, "empty_notification")
        }

        val normalizedText = normalizeForMatching(combinedText)
        val lowerText = normalizedText.lowercase()
        val hasAmount = amountPattern.containsMatchIn(normalizedText)
        val hasTransactionKeyword = strongTransactionKeywords.any { lowerText.contains(it) }
        val hasAccountContext = accountPattern.containsMatchIn(normalizedText)
        val looksPromotional = promotionalKeywords.any { lowerText.contains(it) }

        if (!hasAmount) {
            return Decision(false, "missing_amount")
        }
        if (!hasTransactionKeyword) {
            return Decision(false, "missing_transaction_keyword")
        }
        if (looksPromotional && !hasAccountContext) {
            return Decision(false, "promotional_content")
        }

        return Decision(true, "accepted")
    }

    private fun normalizeForMatching(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
