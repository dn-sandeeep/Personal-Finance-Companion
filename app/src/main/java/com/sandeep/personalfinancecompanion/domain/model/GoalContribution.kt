package com.sandeep.personalfinancecompanion.domain.model

data class GoalContribution(
    val id: String,
    val amount: Double,
    val date: Long // Unix timestamp in millis
)
