package com.sandeep.personalfinancecompanion.domain.usecase

import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.NoSpendStreak
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetNoSpendStreakUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val NON_ESSENTIAL_CATEGORIES = listOf(
        Category.FOOD,
        Category.SHOPPING,
        Category.ENTERTAINMENT,
        Category.GIFT,
        Category.OTHER
    )

    operator fun invoke(): Flow<NoSpendStreak> {
        return combine(
            repository.getAllTransactions(),
            preferencesRepository.noSpendTargetDaysFlow
        ) { transactions, targetDays ->
            val nonEssentialSpendDates = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .filter { it.category in NON_ESSENTIAL_CATEGORIES }
                .map { getStartOfDay(it.date) }
                .toSet()

            val today = getStartOfDay(System.currentTimeMillis())
            var currentDay = today
            var streak = 0
            val oneDayMillis = 24 * 60 * 60 * 1000L

            // Check if user spent money TODAY
            if (today in nonEssentialSpendDates) {
                return@combine NoSpendStreak(0, targetDays, "Spent today! Restart tomorrow.")
            }

            // Count backwards from yesterday
            currentDay -= oneDayMillis
            while (currentDay !in nonEssentialSpendDates && streak < 365) {
                streak++
                currentDay -= oneDayMillis
            }

            NoSpendStreak(
                currentStreak = streak,
                targetDays = targetDays,
                message = when {
                    streak == 0 -> "Start your journey today!"
                    streak >= targetDays -> "Challenge Completed! 🏆"
                    streak < 3 -> "Great start! Keep it up."
                    streak < 7 -> "You're on fire! 🔥"
                    else -> "Financial Legend! 🏆"
                }
            )
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
