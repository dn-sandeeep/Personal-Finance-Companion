package com.sandeep.personalfinancecompanion.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.sandeep.personalfinancecompanion.domain.parser.SmartTransactionParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var parser: SmartTransactionParser

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var preferencesRepository: com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository

    @Inject
    lateinit var transactionRepository: com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("SmsReceiver", "onReceive triggered with action: ${intent.action}")
        
        if (intent.action == "ACTION_UNDO_TRANSACTION") {
            val transactionId = intent.getStringExtra("EXTRA_TRANSACTION_ID") ?: return
            val pendingResult = goAsync()
            scope.launch {
                try {
                    transactionRepository.deleteTransaction(transactionId)
                    Log.d("SmsReceiver", "Transaction undone: $transactionId")
                    // Optionally dismiss notification or show a toast
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager)
                        .cancel(NotificationHelper.SMS_TRANSACTION_NOTIFICATION_ID)
                } catch (e: Exception) {
                    Log.e("SmsReceiver", "Error undoing transaction", e)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                val isEnabled = com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository::smsDetectionEnabledFlow.let { 
                    preferencesRepository.smsDetectionEnabledFlow.first()
                }
                
                if (!isEnabled) return@launch

                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (message in messages) {
                    val body = message.messageBody ?: continue
                    val sender = message.displayOriginatingAddress ?: ""

                    Log.d("SmsReceiver", "Received SMS from $sender: $body")

                    if (isBankSms(sender, body)) {
                        processSms(context, body)
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error in SmsReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isBankSms(sender: String, body: String): Boolean {
        val lowerBody = body.lowercase()
        
        // 1. Improved Sender Check: Most banks use 6-character alphanumeric IDs (e.g., HDFCBK, SBINB)
        // or have a prefix with a hyphen (e.g., AD-HDFCBK)
        val isBankSender = sender.length >= 3 && (sender.contains("-") || sender.any { it.isDigit() } || sender.all { it.isUpperCase() })
        
        // 2. Comprehensive Financial Keywords
        val financialKeywords = listOf(
            "debited", "credited", "spent", "paid", "transaction", "txn", "vpa", "upi", "bank", 
            "a/c", "acct", "account", "card", "spent", "withdrawn", "received", "amt", "inr", "rs"
        )
        val hasKeywords = financialKeywords.any { lowerBody.contains(it) }

        // 3. Improved Account/Card context
        val hasAccountContext = lowerBody.contains(Regex("""(?i)(a/c|card|acct|acc|account|ending|ending in)\s*[x*]*\d+"""))

        // 4. Money Pattern check
        val hasMoney = lowerBody.contains(Regex("""(?i)(rs|inr|amt)\.?\s*\d+""")) || lowerBody.contains(Regex("""\d+\.\d{2}"""))

        return (isBankSender || hasKeywords) && (hasAccountContext || hasMoney)
    }

    private fun processSms(context: Context, body: String) {
        scope.launch {
            try {
                val parsedResults = parser.parse(context, body)
                if (parsedResults.isNotEmpty()) {
                    val result = parsedResults.first()
                    
                    val isAutoSaveEnabled = preferencesRepository.autoSaveSmsTransactionsFlow.first()

                    if (isAutoSaveEnabled) {
                        // Create and save transaction directly
                        val transactionId = java.util.UUID.randomUUID().toString()
                        val transaction = com.sandeep.personalfinancecompanion.domain.model.Transaction(
                            id = transactionId,
                            amount = result.amount ?: 0.0,
                            category = result.category,
                            type = result.type,
                            notes = result.notes,
                            date = result.date.time
                        )
                        transactionRepository.addTransaction(transaction)
                        
                        // Show notification with Undo/Edit
                        notificationHelper.showTransactionAutoSaved(
                            transactionId = transactionId,
                            amount = result.amount ?: 0.0,
                            type = result.type.name,
                            merchant = result.notes
                        )
                    } else {
                        // Show notification for manual confirmation
                        notificationHelper.showTransactionDetected(
                            amount = result.amount ?: 0.0,
                            type = result.type.name,
                            merchant = result.notes
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error processing SMS", e)
            }
        }
    }
}
