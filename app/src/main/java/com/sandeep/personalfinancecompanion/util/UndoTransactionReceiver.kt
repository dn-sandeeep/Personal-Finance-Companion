package com.sandeep.personalfinancecompanion.util

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UndoTransactionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_UNDO_TRANSACTION) return

        val transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID) ?: return
        val pendingResult = goAsync()
        scope.launch {
            try {
                transactionRepository.deleteTransaction(transactionId)
                Log.d(TAG, "Transaction undone: $transactionId")
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .cancel(NotificationHelper.SMS_TRANSACTION_NOTIFICATION_ID)
            } catch (e: Exception) {
                Log.e(TAG, "Error undoing transaction", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_UNDO_TRANSACTION = "ACTION_UNDO_TRANSACTION"
        const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"
        private const val TAG = "UndoTransactionReceiver"
    }
}
