package com.sandeep.personalfinancecompanion.appfunctions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.service.AppFunctionConfiguration
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service that handles App Function requests from the Android System.
 * Hilt is used to inject the actual implementation class and provide it via a factory.
 */
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@AndroidEntryPoint
class FinanceAppFunctionService : AppFunctionService(), AppFunctionConfiguration.Provider {

    @Inject
    lateinit var financeAppFunctions: FinanceAppFunctions

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(FinanceAppFunctions::class.java) {
                financeAppFunctions
            }
            .build()

    override suspend fun executeFunction(
        request: ExecuteAppFunctionRequest
    ): ExecuteAppFunctionResponse {
        // In 1.0.0-alpha08, the library delegates the execution to the generated code
        // based on the AppFunctionConfiguration.Provider we implemented.
        throw UnsupportedOperationException("Use compiler generated dispatch path")
    }
}
