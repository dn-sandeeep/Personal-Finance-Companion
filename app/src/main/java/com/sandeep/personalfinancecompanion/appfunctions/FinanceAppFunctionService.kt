package com.sandeep.personalfinancecompanion.appfunctions

import androidx.appfunctions.service.AppFunctionService
import androidx.appfunctions.service.AppFunctionConfiguration
import androidx.appfunctions.service.ExecuteAppFunctionRequest
import androidx.appfunctions.service.ExecuteAppFunctionResponse
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service that handles App Function requests from the Android System.
 * Hilt is used to inject the actual implementation class and provide it via a factory.
 */
@AndroidEntryPoint
class FinanceAppFunctionService : AppFunctionService(), AppFunctionConfiguration.Provider {

    @Inject
    lateinit var financeAppFunctions: FinanceAppFunctions

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(FinanceAppFunctions::class) {
                financeAppFunctions
            }
            .build()

    override fun executeFunction(request: ExecuteAppFunctionRequest): ExecuteAppFunctionResponse {
        // By using the configuration provider with a factory, 
        // the library's internal logic handles the dispatch.
        // We call super or throw an exception if we want to delegate to the generated invoker.
        return super.executeFunction(request)
    }
}
