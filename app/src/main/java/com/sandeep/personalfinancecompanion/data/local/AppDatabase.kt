package com.sandeep.personalfinancecompanion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao
import com.sandeep.personalfinancecompanion.data.local.dao.GoalDao
import com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity

@Database(
    entities = [TransactionEntity::class, GoalEntity::class, GoalContributionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract val transactionDao: TransactionDao
    abstract val goalDao: GoalDao
}
