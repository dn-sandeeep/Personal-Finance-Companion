package com.sandeep.personalfinancecompanion.domain.model

data class Goal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val iconName: String,
    val colorHex: String,
    val contributions: List<GoalContribution>,
    val targetDate: Long? = null
) {
    val progress: Float
        get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val isOverdue: Boolean
        get() = targetDate != null && System.currentTimeMillis() > targetDate && savedAmount < targetAmount

    val daysRemaining: Int?
        get() = targetDate?.let {
            val diff = it - System.currentTimeMillis()
            (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        }
}
