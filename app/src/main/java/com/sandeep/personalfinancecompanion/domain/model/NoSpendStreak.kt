package com.sandeep.personalfinancecompanion.domain.model

data class NoSpendStreak(
    val currentStreak: Int,
    val message: String,
    val isHealthy: Boolean = true
)
