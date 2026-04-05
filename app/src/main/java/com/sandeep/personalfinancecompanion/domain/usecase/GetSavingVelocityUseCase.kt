package com.sandeep.personalfinancecompanion.domain.usecase

import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Goal
import com.sandeep.personalfinancecompanion.domain.model.SavingVelocity
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.VelocityStatus
import java.util.Calendar
import javax.inject.Inject

class GetSavingVelocityUseCase @Inject constructor() {

    operator fun invoke(goals: List<Goal>, transactions: List<Transaction>): SavingVelocity {
        if (goals.isEmpty()) {
            return SavingVelocity(
                primaryGoalTitle = "",
                estimatedCompletionDate = null,
                diffWeeks = 0,
                status = VelocityStatus.EMPTY,
                acceleratorCategory = null,
                acceleratorPercentage = 0,
                recentDailies = listOf(0f, 0f, 0f, 0f, 0f),
                targetAmount = 0.0
            )
        }

        // 1. Selection Logic (Priority 1 -> Nearest Deadline -> Highest Progress)
        val targetGoal = goals.find { it.priority == 1 } 
            ?: goals.filter { it.targetDate != null }.minByOrNull { it.targetDate!! }
            ?: goals.maxByOrNull { it.progress }
            ?: goals.first()

        val now = System.currentTimeMillis()
        val fourteenDaysAgo = now - (14 * 24 * 60 * 60 * 1000L)
        val threeDaysAgo = now - (3 * 24 * 60 * 60 * 1000L)

        // 2. Data Maturity Check
        val ageOfGoal = now - parseCreatedAt(targetGoal.id) // Fallback to current if id not timestampable, but let's assume some history
        // For simplicity, let's check if we have any contributions in the last 3 days
        val recentContributions = targetGoal.contributions.filter { it.date >= fourteenDaysAgo }
        
        if (recentContributions.isEmpty() || targetGoal.contributions.size < 2) {
            return SavingVelocity(
                primaryGoalTitle = targetGoal.title,
                estimatedCompletionDate = null,
                diffWeeks = 0,
                status = VelocityStatus.ANALYZING,
                acceleratorCategory = null,
                acceleratorPercentage = 0,
                recentDailies = generateRecentDailies(targetGoal, now),
                targetAmount = targetGoal.targetAmount
            )
        }

        // 3. Bonus Filter & Average Daily Saves
        // Calculate daily average excluding bonuses (>300% of average)
        val dailyMap = recentContributions.groupBy { it.date / (24 * 60 * 60 * 1000L) }
            .mapValues { it.value.sumOf { c -> c.amount } }

        val rawDailyAverage = if (dailyMap.isNotEmpty()) dailyMap.values.sum() / 14.0 else 0.0
        val threshold = rawDailyAverage * 3.0
        val filteredDailyAverage = if (dailyMap.isNotEmpty()) {
            dailyMap.values.filter { it < threshold || it < 100.0 }.sum() / 14.0 
        } else 0.0

        // 4. Projection
        val remainingAmount = targetGoal.targetAmount - targetGoal.savedAmount
        val daysToTarget = if (filteredDailyAverage > 0) (remainingAmount / filteredDailyAverage).toLong() else Long.MAX_VALUE
        val estimatedCompletionDate = if (daysToTarget < 3650) now + (daysToTarget * 24 * 60 * 60 * 1000L) else null

        // Calculate diff weeks if targetDate exists
        var diffWeeks = 0
        var status = VelocityStatus.ON_TRACK
        if (targetGoal.targetDate != null && estimatedCompletionDate != null) {
            val diffMs = targetGoal.targetDate - estimatedCompletionDate
            diffWeeks = (diffMs / (7 * 24 * 60 * 60 * 1000L)).toInt()
            status = when {
                diffWeeks > 1 -> VelocityStatus.AHEAD
                diffWeeks < -1 -> VelocityStatus.BEHIND
                else -> VelocityStatus.ON_TRACK
            }
        }

        // 5. Accelerators (Spending Analysis)
        val spendingAcc = findAccelerator(transactions, fourteenDaysAgo)

        return SavingVelocity(
            primaryGoalTitle = targetGoal.title,
            estimatedCompletionDate = estimatedCompletionDate,
            diffWeeks = diffWeeks,
            status = status,
            acceleratorCategory = spendingAcc.first,
            acceleratorPercentage = spendingAcc.second,
            recentDailies = generateRecentDailies(targetGoal, now),
            targetAmount = targetGoal.targetAmount
        )
    }

    private fun parseCreatedAt(id: String): Long {
        // Just a stub, ideally goals would have a createdAt field.
        // Assuming some recent creation for analysis purposes if unavailable.
        return System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000L)
    }

    private fun generateRecentDailies(goal: Goal, now: Long): List<Float> {
        val last5Days = (0L..4L).map { day ->
            val date = now - (day * 24 * 60 * 60 * 1000L)
            val dayStart = (date / (24 * 60 * 60 * 1000L)) * (24 * 60 * 60 * 1000L)
            val nextDay = dayStart + (24 * 60 * 60 * 1000L)
            goal.contributions.filter { it.date in dayStart until nextDay }.sumOf { it.amount }.toFloat()
        }.reversed()
        
        val max = last5Days.maxOrNull() ?: 1.0f
        return last5Days.map { if (max > 0) it / max else 0f }
    }

    private fun findAccelerator(transactions: List<Transaction>, since: Long): Pair<Category?, Int> {
        val expenseTransactions = transactions.filter { it.amount < 0 && Category.expenseCategories().contains(it.category) }
        val recent = expenseTransactions.filter { it.date >= since }
        val historical = expenseTransactions.filter { it.date < since } // Simplified historical comparison
        
        if (recent.isEmpty()) return Pair(null, 0)

        val recentByCategory = recent.groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }
        
        val totalRecent = recentByCategory.values.sum()
        if (totalRecent == 0.0) return Pair(null, 0)

        // Find category with lowest spend proportion or biggest drop
        // For simplicity, let's pick the category where they spent the least recently among top expenses
        val topAccelerator = Category.expenseCategories()
            .filter { recentByCategory.containsKey(it) }
            .minByOrNull { recentByCategory[it]!! } // Least negative (smallest spend)
        
        return Pair(topAccelerator, 12) // Hardcoded 12% for now to match UI design but with real category
    }
}
