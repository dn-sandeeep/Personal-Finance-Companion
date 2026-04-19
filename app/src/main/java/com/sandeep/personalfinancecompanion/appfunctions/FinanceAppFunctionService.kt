package com.sandeep.personalfinancecompanion.appfunctions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.service.AppFunctionConfiguration
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Service that handles App Function requests from the Android System.
 * We use a manual EntryPoint instead of @AndroidEntryPoint to ensure
 * maximum compatibility with the system agent's lifecycle.
 */
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
class FinanceAppFunctionService : AppFunctionService(), AppFunctionConfiguration.Provider {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FinanceAppFunctionsEntryPoint {
        fun financeAppFunctions(): FinanceAppFunctions
    }

    private val financeAppFunctions: FinanceAppFunctions by lazy {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext, 
            FinanceAppFunctionsEntryPoint::class.java
        )
        entryPoint.financeAppFunctions()
    }

    init {
        Log.d("AI_AGENT", "FinanceAppFunctionService initialized - Ready for Gemini connection")
    }

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() {
            Log.d("AI_AGENT", "Providing AppFunctionConfiguration to the system")
            return AppFunctionConfiguration.Builder()
                .addEnclosingClassFactory(FinanceAppFunctions::class.java) {
                    financeAppFunctions
                }
                .build()
        }

    override suspend fun executeFunction(
        request: ExecuteAppFunctionRequest
    ): ExecuteAppFunctionResponse {
        // In 1.0.0-alpha08, the library delegates the execution to the generated code
        // based on the AppFunctionConfiguration.Provider we implemented.
        throw UnsupportedOperationException("Use compiler generated dispatch path")
    }
}
