package com.sandeep.personalfinancecompanion.domain.usecase

import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.NoSpendStreak
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
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
            preferencesRepository.noSpendTargetFlow,
            preferencesRepository.currencyFlow
        ) { transactions, target, currency ->
            val nonEssentialTransactions = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .filter { it.category in NON_ESSENTIAL_CATEGORIES }

            val nonEssentialSpendDates = nonEssentialTransactions
                .map { getStartOfDay(it.date) }
                .toSet()

            val today = getStartOfDay(System.currentTimeMillis())
            val oneDayMillis = 24 * 60 * 60 * 1000L

            // 1. Calculate Current Streak
            var currentStreak = 0
            val hasSpentToday = today in nonEssentialSpendDates
            
            if (!hasSpentToday) {
                var checkDay = today - oneDayMillis
                while (checkDay !in nonEssentialSpendDates && currentStreak < 365) {
                    currentStreak++
                    checkDay -= oneDayMillis
                }
            }

            // 2. Calculate Best Streak
            var bestStreak = currentStreak
            if (nonEssentialSpendDates.isNotEmpty()) {
                val sortedDates = nonEssentialSpendDates.sortedDescending()
                
                val allDates = transactions.map { getStartOfDay(it.date) }.distinct().sorted()
                if (allDates.isNotEmpty()) {
                    var currentMax = 0
                    var runningStreak = 0
                    var datePtr = allDates.first()
                    val lastPossibleDate = if (hasSpentToday) today - oneDayMillis else today
                    
                    while (datePtr <= lastPossibleDate) {
                        if (datePtr !in nonEssentialSpendDates) {
                            runningStreak++
                        } else {
                            currentMax = maxOf(currentMax, runningStreak)
                            runningStreak = 0
                        }
                        datePtr += oneDayMillis
                    }
                    bestStreak = maxOf(currentMax, runningStreak, currentStreak)
                }
            }

            // 3. Potential Savings (Estimate)
            // Avg non-essential spend per day when spending occurs
            val totalNonEssentialAmount = nonEssentialTransactions.sumOf { it.amount }
            val totalDaysWithNonEssentialSpend = nonEssentialSpendDates.size.toDouble()
            val avgSpendPerDay = if (totalDaysWithNonEssentialSpend > 0) {
                totalNonEssentialAmount / totalDaysWithNonEssentialSpend
            } else {
                // Treated as INR 500 fallback and converted
                val fallbackInINR = 500.0
                com.sandeep.personalfinancecompanion.domain.model.Currency.convert(
                    fallbackInINR,
                    com.sandeep.personalfinancecompanion.domain.model.Currency.INR,
                    currency
                )
            }

            val potentialSavings = currentStreak * avgSpendPerDay

            // 4. Calendar Days (Last 30 days)
            val noSpendDaysList = mutableListOf<Long>()
            for (i in 0 until 30) {
                val day = today - (i * oneDayMillis)
                if (day !in nonEssentialSpendDates) {
                    noSpendDaysList.add(day)
                }
            }

            NoSpendStreak(
                currentStreak = currentStreak,
                message = when {
                    hasSpentToday -> "Spent today! Restart tomorrow."
                    currentStreak == 0 -> "Start your journey today!"
                    currentStreak < 3 -> "Great start! Keep it up."
                    currentStreak < 7 -> "You're on fire! 🔥"
                    currentStreak >= target -> "Challenge Completed! 🏆"
                    else -> "Financial Legend! 🏆"
                },
                isHealthy = !hasSpentToday,
                targetDays = target,
                bestStreak = bestStreak,
                potentialSavings = potentialSavings,
                isCompleted = currentStreak >= target,
                hasSpentToday = hasSpentToday,
                noSpendDays = noSpendDaysList
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
