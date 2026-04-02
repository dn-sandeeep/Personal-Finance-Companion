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

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun getGoalsCount(): Int

    @Transaction
    suspend fun populateIfEmpty() {
        if (getGoalsCount() == 0) {
            val goal1Id = "goal_emergency"
            val goal2Id = "goal_travel"

            insertGoal(GoalEntity(
                goal1Id, "Emergency Fund", 10000.0, 2500.0, "Security", "#4CAF50"
            ))
            insertGoal(GoalEntity(
                goal2Id, "Travel Fund", 5000.0, 1200.0, "FlightTakeoff", "#2196F3"
            ))

            insertContribution(GoalContributionEntity(
                "c1", goal1Id, 1500.0, System.currentTimeMillis() - 86400000 * 2
            ))
            insertContribution(GoalContributionEntity(
                "c2", goal1Id, 1000.0, System.currentTimeMillis() - 86400000
            ))
        }
    }
}
