package com.sandeep.personalfinancecompanion.domain.model

data class Goal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val iconName: String, // Name of the icon to be used
    val colorHex: String // Hex string for the color
) {
    val progress: Float
        get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
}
