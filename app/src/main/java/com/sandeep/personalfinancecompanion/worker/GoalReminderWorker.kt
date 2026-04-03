package com.sandeep.personalfinancecompanion.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sandeep.personalfinancecompanion.domain.repository.GoalRepository
import com.sandeep.personalfinancecompanion.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

class GoalReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val goalRepository: GoalRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val goals = goalRepository.getAllGoals().first()
        val incompleteGoals = goals.filter { it.progress < 1.0f }

        if (incompleteGoals.isNotEmpty()) {
            // Pick one goal to remind about or send a summary
            if (incompleteGoals.size == 1) {
                val goal = incompleteGoals.first()
                val progressPercent = (goal.progress * 100).roundToInt()
                notificationHelper.showGoalReminder(
                    goal.title,
                    "You've saved $progressPercent% so far. Keep going!"
                )
            } else {
                notificationHelper.showGoalReminder(
                    "Multiple Goals",
                    "You have ${incompleteGoals.size} active goals. Don't forget to contribute today!"
                )
            }
        }

        return Result.success()
    }
}
