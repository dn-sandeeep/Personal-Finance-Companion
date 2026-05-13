package com.sandeep.personalfinancecompanion.util

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.parser.SmartTransactionParser
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class TransactionNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var parser: SmartTransactionParser

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        Log.d("NotificationListener", "Received notification from $packageName: $title - $text")

        // Filter for potential finance related notifications
        // Check if it's from a messaging app or a banking app
        if (isPotentialFinanceApp(packageName, title, text)) {
            processNotification(packageName, title, text)
        }
    }

    private fun isPotentialFinanceApp(packageName: String, title: String, text: String): Boolean {
        // Banking and finance apps - always process their notifications
        val bankingApps = listOf(
            "com.pnb.pnbone",                      // PNB One
            "com.sbi.lotusintouch",                 // SBI YONO
            "com.csam.icici.bank.imobile",          // iMobile Pay
            "com.axis.mobile",                      // Axis Mobile
            "net.one97.paytm",                      // Paytm
            "com.phonepe.app",                      // PhonePe
            "com.google.android.apps.nbu.paisa.user", // Google Pay
            "in.amazon.mShop.android.shopping",     // Amazon Pay
            "com.whatsapp"                          // WhatsApp Pay notifications
        )
        
        if (bankingApps.contains(packageName)) return true
        
        // Messaging apps - only process if text has strong financial signals
        val messagingApps = listOf(
            "com.google.android.apps.messaging",    // Google Messages (SMS/RCS)
            "com.samsung.android.messaging"          // Samsung Messages
        )
        
        if (!messagingApps.contains(packageName)) return false
        
        // For messaging apps, require BOTH a financial keyword AND a currency/amount pattern
        val textLower = text.lowercase()
        val hasFinancialKeyword = listOf("debited", "credited", "txn", "vpa", "upi", "a/c", "acct", "neft", "imps", "rtgs")
            .any { textLower.contains(it) }
        val hasAmountPattern = Regex("""(rs\.?|inr|₹)\s*[\d,]+""", RegexOption.IGNORE_CASE).containsMatchIn(text)
        
        return hasFinancialKeyword && hasAmountPattern
    }

    private fun processNotification(packageName: String, title: String, text: String) {
        scope.launch {
            try {
                val parsedResults = parser.parse(applicationContext, text)
                if (parsedResults.isNotEmpty()) {
                    val result = parsedResults.firstOrNull { it.amount != null } ?: return@launch
                    val rawSourceText = listOf(title.trim(), text.trim())
                        .filter { it.isNotBlank() }
                        .joinToString("\n")
                    val fingerprintInput = buildString {
                        append(packageName.trim())
                        append('\n')
                        append(parser.normalizeForMatching(rawSourceText))
                    }
                    val sourceFingerprint = UUID.nameUUIDFromBytes(
                        fingerprintInput.toByteArray(Charsets.UTF_8)
                    ).toString()
                    val transactionId = UUID.randomUUID().toString()
                    val transaction = Transaction(
                        id = transactionId,
                        amount = result.amount ?: 0.0,
                        category = result.category,
                        type = result.type,
                        notes = result.notes,
                        date = result.date.time,
                        sourceType = "notification",
                        sourceFingerprint = sourceFingerprint,
                        rawSourceText = rawSourceText
                    )
                    val inserted = transactionRepository.addAutoImportedTransaction(transaction)
                    if (inserted) {
                        notificationHelper.showTransactionAutoSaved(
                            transactionId = transactionId,
                            amount = result.amount ?: 0.0,
                            type = result.type.name,
                            merchant = result.notes
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationListener", "Error processing notification", e)
            }
        }
    }
}
