package com.sandeep.personalfinancecompanion.data.local.dao

import androidx.room.*
import com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalWithContributions
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Transaction
    @Query("SELECT * FROM goals ORDER BY (CASE WHEN priority = 0 THEN 999 ELSE priority END) ASC, title ASC")
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

    @Query("UPDATE goals SET priority = :priority WHERE id = :id")
    suspend fun updateGoalPriority(id: String, priority: Int)

    @Query("UPDATE goals SET priority = 0 WHERE priority = 3")
    suspend fun clearTertiary()

    @Query("UPDATE goals SET priority = 3 WHERE priority = 2")
    suspend fun shiftSecondaryToTertiary()

    @Query("UPDATE goals SET priority = 2 WHERE priority = 1")
    suspend fun shiftPrimaryToSecondary()

    @Transaction
    suspend fun updatePriorityAndShift(goalId: String, newPriority: Int) {
        if (newPriority == 1) {
            clearTertiary()
            shiftSecondaryToTertiary()
            shiftPrimaryToSecondary()
        } else if (newPriority == 2) {
            clearTertiary()
            shiftSecondaryToTertiary()
        } else if (newPriority == 3) {
            clearTertiary()
        }
        updateGoalPriority(goalId, newPriority)
    }

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: String)

    @Query("DELETE FROM goal_contributions WHERE id = :contributionId")
    suspend fun deleteContribution(contributionId: String)

    @Query("UPDATE goals SET targetAmount = targetAmount * :factor, savedAmount = savedAmount * :factor")
    suspend fun convertAllGoals(factor: Double)

    @Query("UPDATE goal_contributions SET amount = amount * :factor")
    suspend fun convertAllContributions(factor: Double)
}
