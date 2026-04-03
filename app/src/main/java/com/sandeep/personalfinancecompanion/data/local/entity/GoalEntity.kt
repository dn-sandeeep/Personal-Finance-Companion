package com.sandeep.personalfinancecompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val iconName: String,
    val colorHex: String,
    val targetDate: Long? = null
)
