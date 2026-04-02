package com.sandeep.personalfinancecompanion.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class GoalWithContributions(
    @Embedded val goal: GoalEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val contributions: List<GoalContributionEntity>
)
