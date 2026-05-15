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

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeNotifications
            ?.filter { it.packageName != applicationContext.packageName }
            ?.forEach { notification ->
                Log.d(
                    "NotificationListener",
                    "Scanning active notification from ${notification.packageName}"
                )
                handleNotification(notification)
            }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        handleNotification(sbn)
    }

    private fun handleNotification(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (packageName == applicationContext.packageName) return

        val extras = sbn.notification.extras
        val snapshot = NotificationTextExtractor.fromExtras(extras)
        val title = snapshot.title
        val text = NotificationTextExtractor.fromSnapshot(snapshot.copy(title = ""))
        val candidateTexts = NotificationTextExtractor.candidateTextsFromSnapshot(snapshot)

        Log.d(
            "NotificationListener",
            "Received notification from $packageName: $title - $text (segments=${candidateTexts.size})"
        )

        val decision = NotificationTransactionFilter.evaluate(packageName, title, text, candidateTexts)
        if (decision.shouldProcess) {
            processNotification(packageName, snapshot)
        } else {
            Log.d("NotificationListener", "Skipped notification from $packageName: ${decision.reason}")
        }
    }

    private fun processNotification(
        packageName: String,
        snapshot: NotificationTextSnapshot
    ) {
        scope.launch {
            try {
                data class ParsedNotificationEntry(
                    val sourceText: String,
                    val result: com.sandeep.personalfinancecompanion.domain.parser.ParsedTransaction,
                    val isFallback: Boolean
                )

                val normalizedSegments = parseSegments(snapshot)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinctBy { parser.normalizeForMatching(it) }

                val parsedEntries = mutableListOf<ParsedNotificationEntry>()

                for (segment in normalizedSegments) {
                    val parsedResults = parser.parse(
                        applicationContext,
                        segment
                    )
                    parsedEntries += parsedResults
                        .filter { it.amount != null }
                        .map { result -> ParsedNotificationEntry(segment, result, false) }
                }

                if (parsedEntries.isEmpty()) {
                    val fallbackText = listOf(snapshot.title.trim(), textForFingerprint(snapshot)).joinToString(" ").trim()
                    if (fallbackText.isNotBlank()) {
                        val fallbackResults = parser.parse(
                            applicationContext,
                            fallbackText
                        )
                        parsedEntries += fallbackResults
                            .filter { it.amount != null }
                            .map { result -> ParsedNotificationEntry(fallbackText, result, true) }
                    }
                }

                for (entry in parsedEntries) {
                    val rawSourceText = if (entry.isFallback) {
                        entry.sourceText.trim()
                    } else {
                        listOf(snapshot.title.trim(), entry.sourceText.trim())
                            .filter { it.isNotBlank() }
                            .joinToString("\n")
                    }
                    val fingerprintInput = buildString {
                        append(packageName.trim())
                        append('\n')
                        append(parser.normalizeForMatching(rawSourceText))
                        append('\n')
                        append(entry.result.amount ?: 0.0)
                        append('\n')
                        append(entry.result.type.name)
                        append('\n')
                        append(entry.result.category.name)
                    }
                    val sourceFingerprint = UUID.nameUUIDFromBytes(
                        fingerprintInput.toByteArray(Charsets.UTF_8)
                    ).toString()
                    val transactionId = UUID.randomUUID().toString()
                    val transaction = Transaction(
                        id = transactionId,
                        amount = entry.result.amount ?: 0.0,
                        category = entry.result.category,
                        type = entry.result.type,
                        notes = entry.result.notes,
                        date = entry.result.date.time,
                        sourceType = "notification",
                        sourceFingerprint = sourceFingerprint,
                        rawSourceText = rawSourceText
                    )
                    val inserted = transactionRepository.addAutoImportedTransaction(transaction)
                    if (inserted) {
                        notificationHelper.showTransactionAutoSaved(
                            transactionId = transactionId,
                            amount = entry.result.amount ?: 0.0,
                            type = entry.result.type.name,
                            merchant = entry.result.notes
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationListener", "Error processing notification", e)
            }
        }
    }

    private fun textForFingerprint(snapshot: NotificationTextSnapshot): String {
        return NotificationTextExtractor.fromSnapshot(snapshot.copy(title = ""))
    }

    private fun parseSegments(snapshot: NotificationTextSnapshot): List<String> {
        return when {
            snapshot.messageTexts.isNotEmpty() -> snapshot.messageTexts
            snapshot.textLines.isNotEmpty() -> snapshot.textLines
            else -> NotificationTextExtractor.candidateTextsFromSnapshot(snapshot)
        }
    }
}
