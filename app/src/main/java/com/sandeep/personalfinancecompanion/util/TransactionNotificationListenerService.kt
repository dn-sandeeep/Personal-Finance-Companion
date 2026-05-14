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
        if (packageName == applicationContext.packageName) return

        val extras = sbn.notification.extras
        val snapshot = NotificationTextExtractor.fromExtras(extras)
        val title = snapshot.title
        val text = NotificationTextExtractor.fromSnapshot(snapshot.copy(title = ""))

        Log.d("NotificationListener", "Received notification from $packageName: $title - $text")

        val decision = NotificationTransactionFilter.evaluate(packageName, title, text)
        if (decision.shouldProcess) {
            processNotification(packageName, title, text)
        } else {
            Log.d("NotificationListener", "Skipped notification from $packageName: ${decision.reason}")
        }
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
