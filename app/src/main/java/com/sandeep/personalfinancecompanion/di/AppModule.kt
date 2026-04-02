package com.sandeep.personalfinancecompanion.di

import android.app.Application
import androidx.room.Room
import com.sandeep.personalfinancecompanion.data.local.AppDatabase
import com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao
import com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
}
