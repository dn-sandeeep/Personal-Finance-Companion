package com.sandeep.personalfinancecompanion.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.sandeep.personalfinancecompanion.analytics.AnalyticsTracker
import com.sandeep.personalfinancecompanion.analytics.CrashReporter
import com.sandeep.personalfinancecompanion.analytics.FirebaseCrashReporter
import com.sandeep.personalfinancecompanion.analytics.FirebaseAnalyticsTracker
import com.sandeep.personalfinancecompanion.data.local.AppDatabase
import com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao
import com.sandeep.personalfinancecompanion.data.local.dao.GoalDao
import com.sandeep.personalfinancecompanion.data.local.dao.UdhaarDao
import com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl
import com.sandeep.personalfinancecompanion.data.repository.GoalRepositoryImpl
import com.sandeep.personalfinancecompanion.data.repository.UdhaarRepositoryImpl
import com.sandeep.personalfinancecompanion.data.repository.UserPreferencesRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.repository.GoalRepository
import com.sandeep.personalfinancecompanion.domain.repository.UdhaarRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import com.sandeep.personalfinancecompanion.domain.repository.SmartParserRepository
import com.sandeep.personalfinancecompanion.data.repository.SmartParserRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.parser.SmartTransactionParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

    @Provides
    @Singleton
    fun provideAppDatabase(
        app: Application
    ): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "finance_db"
        )
        .addMigrations(AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6)
        .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao
    }

    @Provides
    @Singleton
    fun provideGoalDao(appDatabase: AppDatabase): GoalDao {
        return appDatabase.goalDao
    }

    @Provides
    @Singleton
    fun provideUdhaarDao(appDatabase: AppDatabase): UdhaarDao {
        return appDatabase.udhaarDao
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        dao: TransactionDao
    ): TransactionRepository {
        return TransactionRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideGoalRepository(
        dao: GoalDao
    ): GoalRepository {
        return GoalRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideUdhaarRepository(
        dao: UdhaarDao
    ): UdhaarRepository {
        return UdhaarRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideDataStore(app: Application): DataStore<Preferences> {
        return app.dataStore
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        dataStore: DataStore<Preferences>
    ): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideSmartTransactionParser(): SmartTransactionParser {
        return SmartTransactionParser()
    }

    @Provides
    @Singleton
    fun provideSmartParserRepository(
        parser: SmartTransactionParser
    ): SmartParserRepository {
        return SmartParserRepositoryImpl(parser)
    }

    @Provides
    @Singleton
    fun provideAnalyticsTracker(
        tracker: FirebaseAnalyticsTracker
    ): AnalyticsTracker {
        return tracker
    }

    @Provides
    @Singleton
    fun provideCrashReporter(
        reporter: FirebaseCrashReporter
    ): CrashReporter {
        return reporter
    }
}
