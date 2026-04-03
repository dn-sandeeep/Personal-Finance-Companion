package com.sandeep.personalfinancecompanion.data.mapper

import com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalWithContributions
import com.sandeep.personalfinancecompanion.domain.model.Goal
import com.sandeep.personalfinancecompanion.domain.model.GoalContribution
import java.util.UUID

fun GoalContributionEntity.toDomainModel(): GoalContribution {
    return GoalContribution(
        id = id,
        amount = amount,
        date = date
    )
}

fun GoalContribution.toEntity(goalId: String): GoalContributionEntity {
    return GoalContributionEntity(
        id = id.ifEmpty { UUID.randomUUID().toString() },
        goalId = goalId,
        amount = amount,
        date = date
    )
}

fun GoalWithContributions.toDomainModel(): Goal {
    return Goal(
        id = goal.id,
        title = goal.title,
        targetAmount = goal.targetAmount,
        savedAmount = goal.savedAmount,
        iconName = goal.iconName,
        colorHex = goal.colorHex,
        contributions = contributions.map { it.toDomainModel() },
        targetDate = goal.targetDate
    )
}

fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id.ifEmpty { UUID.randomUUID().toString() },
        title = title,
        targetAmount = targetAmount,
        savedAmount = savedAmount,
        iconName = iconName,
        colorHex = colorHex,
        targetDate = targetDate
    )
}
