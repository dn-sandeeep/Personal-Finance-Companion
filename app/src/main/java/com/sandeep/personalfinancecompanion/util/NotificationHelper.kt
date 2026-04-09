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
        
        const val DAILY_REMINDER_NOTIFICATION_ID = 1001
        const val BUDGET_ALERT_NOTIFICATION_ID = 1002
        const val GOAL_REMINDER_NOTIFICATION_ID = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersChannel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to log your expenses"
            }

            val budgetChannel = NotificationChannel(
                BUDGET_ALERTS_CHANNEL_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you approach or exceed your budget"
            }

            val goalsChannel = NotificationChannel(
                GOAL_ALERTS_CHANNEL_ID,
                "Goal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates on your savings goals"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(remindersChannel)
            manager.createNotificationChannel(budgetChannel)
            manager.createNotificationChannel(goalsChannel)
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
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Replace with app icon later
            .setContentTitle("Finance Reminder")
            .setContentText("Don't forget to log your expenses for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        showNotification(DAILY_REMINDER_NOTIFICATION_ID, notification)
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

    fun showGoalReminder(goalTitle: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val titleText: CharSequence = "Goal Update: $goalTitle"
        val messageText: CharSequence = message
        val builder = NotificationCompat.Builder(context, GOAL_ALERTS_CHANNEL_ID)
        //builder.setSmallIcon(android.R.drawable.ic_menu_flag)
        builder.setContentTitle(titleText)
        builder.setContentText(messageText)
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        showNotification(GOAL_REMINDER_NOTIFICATION_ID, builder.build())
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
