package com.sandeep.personalfinancecompanion.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao
import com.sandeep.personalfinancecompanion.data.local.dao.GoalDao
import com.sandeep.personalfinancecompanion.data.local.dao.UdhaarDao
import com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity
import com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity
import com.sandeep.personalfinancecompanion.data.local.entity.UdhaarEntryEntity
import com.sandeep.personalfinancecompanion.data.local.entity.UdhaarPersonEntity

@Database(
    entities = [
        TransactionEntity::class,
        GoalEntity::class,
        GoalContributionEntity::class,
        UdhaarPersonEntity::class,
        UdhaarEntryEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract val transactionDao: TransactionDao
    abstract val goalDao: GoalDao
    abstract val udhaarDao: UdhaarDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `goals` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `targetAmount` REAL NOT NULL,
                        `savedAmount` REAL NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `goal_contributions` (
                        `id` TEXT NOT NULL,
                        `goalId` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `date` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_goal_contributions_goalId` ON `goal_contributions` (`goalId`)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE goals ADD COLUMN targetDate INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE goals ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN peerName TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN isSettled INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `udhaar_people` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `phoneNumber` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `udhaar_entries` (
                        `id` TEXT NOT NULL,
                        `personId` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `type` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `date` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`personId`) REFERENCES `udhaar_people`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_udhaar_entries_personId` ON `udhaar_entries` (`personId`)"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceType TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceFingerprint TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN rawSourceText TEXT")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_transactions_sourceFingerprint ON transactions(sourceFingerprint)"
                )
            }
        }
    }
}
