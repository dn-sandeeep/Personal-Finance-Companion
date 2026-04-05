package com.sandeep.personalfinancecompanion.domain.model

data class SavingVelocity(
    val primaryGoalTitle: String,
    val estimatedCompletionDate: Long?,
    val diffWeeks: Int, // e.g., 2 weeks early (positive) or 2 weeks late (negative)
    val status: VelocityStatus,
    val acceleratorCategory: Category?,
    val acceleratorPercentage: Int,
    val recentDailies: List<Float>, // Last 5 days of savings for the bar chart
    val targetAmount: Double
)

enum class VelocityStatus {
    AHEAD, ON_TRACK, BEHIND, ANALYZING, EMPTY
}
