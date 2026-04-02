package com.sandeep.personalfinancecompanion.data.local.entity

import androidx.room.*

@Entity(
    tableName = "goal_contributions",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goalId"])]
)
data class GoalContributionEntity(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val amount: Double,
    val date: Long
)
