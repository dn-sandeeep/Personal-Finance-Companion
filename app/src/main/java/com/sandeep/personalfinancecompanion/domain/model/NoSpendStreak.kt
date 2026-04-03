package com.sandeep.personalfinancecompanion.domain.model

data class NoSpendStreak(
    val currentStreak: Int,
    val message: String,
    val isHealthy: Boolean = true,
    val targetDays: Int = 30,
    val bestStreak: Int = 0,
    val potentialSavings: Double = 0.0,
    val isCompleted: Boolean = false,
    val hasSpentToday: Boolean = false,
    val noSpendDays: List<Long> = emptyList()
)
