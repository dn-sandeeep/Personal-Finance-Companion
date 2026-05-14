package com.sandeep.personalfinancecompanion.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sandeep.personalfinancecompanion.MainActivity
import com.sandeep.personalfinancecompanion.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        const val REMINDERS_CHANNEL_ID = "reminders_channel"
        const val BUDGET_ALERTS_CHANNEL_ID = "budget_alerts_channel"
        const val GOAL_ALERTS_CHANNEL_ID = "goal_alerts_channel"
        const val SMS_DETECTION_CHANNEL_ID = "sms_detection_channel"
        
        const val DAILY_REMINDER_NOTIFICATION_ID = 1001
        const val BUDGET_ALERT_NOTIFICATION_ID = 1002
        const val GOAL_REMINDER_NOTIFICATION_ID = 1003
        const val SMS_TRANSACTION_NOTIFICATION_ID = 1004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersChannel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                context.getString(R.string.nt_reminders_title),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.nt_reminders_desc)
            }

            val budgetChannel = NotificationChannel(
                BUDGET_ALERTS_CHANNEL_ID,
                context.getString(R.string.nt_budget_alerts_title),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.nt_budget_alerts_desc)
            }

            val goalsChannel = NotificationChannel(
                GOAL_ALERTS_CHANNEL_ID,
                context.getString(R.string.nt_goal_reminders_title),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.nt_goal_reminders_desc)
            }

            val smsChannel = NotificationChannel(
                SMS_DETECTION_CHANNEL_ID,
                context.getString(R.string.nt_sms_detection_title),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.nt_sms_detection_desc)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(remindersChannel)
            manager.createNotificationChannel(budgetChannel)
            manager.createNotificationChannel(goalsChannel)
            manager.createNotificationChannel(smsChannel)
        }
    }

    fun showDailyReminder() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(context.getString(R.string.nt_daily_reminder_title))
            .setContentText(context.getString(R.string.nt_daily_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        showNotification(DAILY_REMINDER_NOTIFICATION_ID, notification)
    }

    fun showBudgetExceededAlert(totalExpenseFormatted: String, budgetLimitFormatted: String) {
        val title = context.getString(R.string.nt_budget_exceeded_title)
        val message = context.getString(R.string.nt_budget_exceeded_text, totalExpenseFormatted, budgetLimitFormatted)
        showBudgetAlert(title, message)
    }

    fun showBudgetWarningAlert(percent: String, spentFormatted: String, limitFormatted: String) {
        val title = context.getString(R.string.nt_budget_warning_title)
        val message = context.getString(R.string.nt_budget_warning_text, percent, spentFormatted, limitFormatted)
        showBudgetAlert(title, message)
    }

    fun showBudgetAlert(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, BUDGET_ALERTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        showNotification(BUDGET_ALERT_NOTIFICATION_ID, notification)
    }

    fun showGoalProgressReminder(goalTitle: String, progressPercent: Int) {
        val titleText = context.getString(R.string.nt_goal_update_title, goalTitle)
        val message = context.getString(R.string.nt_goal_reminder_single, progressPercent)
        showGoalReminder(titleText, message)
    }

    fun showMultipleGoalsReminder(activeGoalsCount: Int) {
        val titleText = context.getString(R.string.nt_goal_reminder_multiple_title)
        val message = context.getString(R.string.nt_goal_reminder_multiple_text, activeGoalsCount)
        showGoalReminder(titleText, message)
    }

    fun showGoalReminder(titleText: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, GOAL_ALERTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titleText)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        showNotification(GOAL_REMINDER_NOTIFICATION_ID, builder.build())
    }

    fun showTransactionDetected(amount: Double, type: String, merchant: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_AMOUNT", amount)
            putExtra("EXTRA_TYPE", type)
            putExtra("EXTRA_MERCHANT", merchant)
            putExtra("FROM_SMS", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountFormatted = "₹${amount}"
        val message = context.getString(R.string.nt_sms_transaction_text, amountFormatted, type)

        val notification = NotificationCompat.Builder(context, SMS_DETECTION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(context.getString(R.string.nt_sms_transaction_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\nMerchant: $merchant"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        showNotification(SMS_TRANSACTION_NOTIFICATION_ID, notification)
    }

    fun showTransactionAutoSaved(transactionId: String, amount: Double, type: String, merchant: String) {
        // Main intent to open and edit
        val editIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_AMOUNT", amount)
            putExtra("EXTRA_TYPE", type)
            putExtra("EXTRA_MERCHANT", merchant)
            putExtra("FROM_SMS", true)
        }
        val editPendingIntent = PendingIntent.getActivity(
            context, 4, editIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Undo intent to delete
        val undoIntent = Intent(context, UndoTransactionReceiver::class.java).apply {
            action = UndoTransactionReceiver.ACTION_UNDO_TRANSACTION
            putExtra(UndoTransactionReceiver.EXTRA_TRANSACTION_ID, transactionId)
        }
        val undoPendingIntent = PendingIntent.getBroadcast(
            context, 5, undoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountFormatted = "₹${amount}"
        val message = "Automatically saved: $amountFormatted $type at $merchant"

        val notification = NotificationCompat.Builder(context, SMS_DETECTION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Transaction Auto-Saved")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(editPendingIntent)
            .addAction(android.R.drawable.ic_menu_edit, "Edit", editPendingIntent)
            .addAction(android.R.drawable.ic_menu_revert, "Undo", undoPendingIntent)
            .setAutoCancel(true)
            .build()

        showNotification(SMS_TRANSACTION_NOTIFICATION_ID, notification)
    }

    private fun showNotification(id: Int, notification: android.app.Notification) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(id, notification)
        }
    }
}
