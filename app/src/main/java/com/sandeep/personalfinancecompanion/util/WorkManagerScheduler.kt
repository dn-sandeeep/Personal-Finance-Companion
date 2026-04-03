package com.sandeep.personalfinancecompanion.util

import android.content.Context
import androidx.work.*
import com.sandeep.personalfinancecompanion.worker.DailyReminderWorker
import com.sandeep.personalfinancecompanion.worker.GoalReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val initialDelay = calendar.timeInMillis - now

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelDailyReminder() {
        workManager.cancelUniqueWork("daily_reminder")
    }

    fun scheduleGoalReminders() {
        val request = PeriodicWorkRequestBuilder<GoalReminderWorker>(7, TimeUnit.DAYS)
            .addTag("goal_reminders")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "goal_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelGoalReminders() {
        workManager.cancelUniqueWork("goal_reminders")
    }
}
