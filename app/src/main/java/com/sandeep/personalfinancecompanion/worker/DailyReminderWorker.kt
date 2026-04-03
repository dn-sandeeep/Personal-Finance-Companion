package com.sandeep.personalfinancecompanion.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sandeep.personalfinancecompanion.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notificationHelper.showDailyReminder()
        return Result.success()
    }
}
