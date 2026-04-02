package com.sandeep.personalfinancecompanion.data.local.dao

import androidx.room.*
import com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalWithContributions
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Transaction
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalWithContributions>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalWithContributionsById(id: String): GoalWithContributions?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: GoalContributionEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Transaction
    suspend fun addContributionAndUpdateGoal(contribution: GoalContributionEntity, updatedGoal: GoalEntity) {
        insertContribution(contribution)
        updateGoal(updatedGoal)
    }

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: String)

    @Query("DELETE FROM goal_contributions WHERE id = :contributionId")
    suspend fun deleteContribution(contributionId: String)
}
