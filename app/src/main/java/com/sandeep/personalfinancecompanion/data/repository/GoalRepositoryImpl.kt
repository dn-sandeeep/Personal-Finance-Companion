package com.sandeep.personalfinancecompanion.data.repository

import com.sandeep.personalfinancecompanion.data.local.dao.GoalDao
import com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity
import com.sandeep.personalfinancecompanion.data.mapper.toDomainModel
import com.sandeep.personalfinancecompanion.data.mapper.toEntity
import com.sandeep.personalfinancecompanion.domain.model.Goal
import com.sandeep.personalfinancecompanion.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class GoalRepositoryImpl(
    private val dao: GoalDao
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> {
        return dao.getAllGoals().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun insertGoal(goal: Goal) {
        dao.insertGoal(goal.toEntity())
    }

    override suspend fun addContribution(goalId: String, amount: Double) {
        val goalWithContributions = dao.getGoalWithContributionsById(goalId) 
        if (goalWithContributions != null) {
            val contribution = GoalContributionEntity(
                id = UUID.randomUUID().toString(),
                goalId = goalId,
                amount = amount,
                date = System.currentTimeMillis()
            )
            val updatedGoal = goalWithContributions.goal.copy(
                savedAmount = goalWithContributions.goal.savedAmount + amount
            )
            dao.addContributionAndUpdateGoal(contribution, updatedGoal)
        }
    }

    override suspend fun updateGoalTargetDate(goalId: String, targetDate: Long?) {
        val goalWithContributions = dao.getGoalWithContributionsById(goalId)
        if (goalWithContributions != null) {
            val updatedGoal = goalWithContributions.goal.copy(
                targetDate = targetDate
            )
            dao.updateGoal(updatedGoal)
        }
    }

    override suspend fun updateGoalSettings(goalId: String, targetAmount: Double, targetDate: Long?) {
        val goalWithContributions = dao.getGoalWithContributionsById(goalId)
        if (goalWithContributions != null) {
            val updatedGoal = goalWithContributions.goal.copy(
                targetAmount = targetAmount,
                targetDate = targetDate
            )
            dao.updateGoal(updatedGoal)
        }
    }

    override suspend fun updateGoalPriority(goalId: String, newPriority: Int) {
        dao.updatePriorityAndShift(goalId, newPriority)
    }

    override suspend fun deleteGoal(id: String) {
        dao.deleteGoal(id)
    }

    override suspend fun convertAllGoalsAndContributions(factor: Double) {
        dao.convertAllGoals(factor)
        dao.convertAllContributions(factor)
    }
}
