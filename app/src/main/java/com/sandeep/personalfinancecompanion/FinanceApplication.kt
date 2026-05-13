package com.sandeep.personalfinancecompanion

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FinanceApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        appScope.launch {
            val cleanupCompleted = preferencesRepository.may132026NotificationCleanupCompletedFlow.first()
            if (!cleanupCompleted) {
                transactionRepository.cleanupBrokenAutoImportedTransactionsForMay132026()
                preferencesRepository.updateMay132026NotificationCleanupCompleted(true)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
