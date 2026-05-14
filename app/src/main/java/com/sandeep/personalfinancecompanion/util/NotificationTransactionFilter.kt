package com.sandeep.personalfinancecompanion.util

object NotificationTransactionFilter {
    private val bankingApps = setOf(
        "com.pnb.pnbone",
        "com.sbi.lotusintouch",
        "com.csam.icici.bank.imobile",
        "com.axis.mobile",
        "net.one97.paytm",
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "in.amazon.mShop.android.shopping"
    )

    private val messagingApps = setOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging"
    )

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
        "received",
        "sent",
        "deposited",
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

    fun shouldProcess(packageName: String, title: String, text: String): Boolean {
        return evaluate(packageName, title, text).shouldProcess
    }

    fun evaluate(packageName: String, title: String, text: String): Decision {
        if (packageName !in bankingApps && packageName !in messagingApps) {
            return Decision(false, "package_not_allowed")
        }

        val combinedText = listOf(title.trim(), text.trim())
            .filter { it.isNotBlank() }
            .joinToString(" ")
        if (combinedText.isBlank()) {
            return Decision(false, "empty_notification")
        }

        val lowerText = combinedText.lowercase()
        val hasAmount = amountPattern.containsMatchIn(combinedText)
        val hasTransactionKeyword = strongTransactionKeywords.any { lowerText.contains(it) }
        val hasAccountContext = accountPattern.containsMatchIn(combinedText)
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
}
