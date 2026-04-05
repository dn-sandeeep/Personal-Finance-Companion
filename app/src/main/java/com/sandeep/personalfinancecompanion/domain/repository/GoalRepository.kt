package com.sandeep.personalfinancecompanion.domain.repository

import com.sandeep.personalfinancecompanion.domain.model.Goal
import com.sandeep.personalfinancecompanion.domain.model.GoalContribution
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    suspend fun insertGoal(goal: Goal)
    suspend fun addContribution(goalId: String, amount: Double)
    suspend fun updateGoalTargetDate(goalId: String, targetDate: Long?)
    suspend fun updateGoalSettings(goalId: String, targetAmount: Double, targetDate: Long?)
    suspend fun updateGoalPriority(goalId: String, newPriority: Int)
    suspend fun deleteGoal(id: String)
    suspend fun convertAllGoalsAndContributions(factor: Double)
}
