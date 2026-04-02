package com.sandeep.personalfinancecompanion.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.sandeep.personalfinancecompanion.data.local.AppDatabase
import com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao
import com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl
import com.sandeep.personalfinancecompanion.data.repository.UserPreferencesRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
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
        app: Application,
        provider: Provider<AppDatabase>
    ): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "finance_db"
        )
        .addCallback(AppDatabase.Callback(provider))
        .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao
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
}
