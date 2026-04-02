package com.sandeep.personalfinancecompanion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao
import com.sandeep.personalfinancecompanion.data.local.dao.GoalDao
import com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider

@Database(
    entities = [TransactionEntity::class, GoalEntity::class, GoalContributionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract val transactionDao: TransactionDao
    abstract val goalDao: GoalDao

    class Callback(
        private val databaseProvider: Provider<AppDatabase>
    ) : RoomDatabase.Callback() {
        
        private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            applicationScope.launch {
                populateDatabase(databaseProvider.get().transactionDao)
            }
        }
        
        private suspend fun populateDatabase(dao: TransactionDao) {
            val dummyTransactions = listOf(
                TransactionEntity("1", 55000.0, "INCOME", "SALARY", 1743465600000, "March salary"),
                TransactionEntity("2", 1200.0, "EXPENSE", "FOOD", 1743552000000, "Groceries at BigBasket"),
                TransactionEntity("3", 500.0, "EXPENSE", "TRANSPORT", 1743638400000, "Uber to office"),
                TransactionEntity("4", 2500.0, "EXPENSE", "SHOPPING", 1743724800000, "Amazon order"),
                TransactionEntity("5", 800.0, "EXPENSE", "ENTERTAINMENT", 1743811200000, "Movie night"),
                TransactionEntity("6", 3000.0, "EXPENSE", "BILLS", 1743897600000, "Electricity bill"),
                TransactionEntity("7", 15000.0, "INCOME", "FREELANCE", 1743984000000, "Client project payment"),
                TransactionEntity("8", 450.0, "EXPENSE", "FOOD", 1744070400000, "Dinner at restaurant"),
                TransactionEntity("9", 1500.0, "EXPENSE", "HEALTH", 1744156800000, "Medicine and consultation"),
                TransactionEntity("10", 5000.0, "INCOME", "INVESTMENT", 1744243200000, "Dividend income")
            )
            dummyTransactions.forEach { dao.insertTransaction(it) }
        }
    }
}
