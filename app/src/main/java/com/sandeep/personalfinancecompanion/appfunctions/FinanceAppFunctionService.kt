package com.sandeep.personalfinancecompanion.appfunctions

import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.appfunctions.service.AppFunctionConfiguration
import androidx.appfunctions.service.AppFunctionException
import androidx.appfunctions.service.AppFunctionService
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

    override fun onExecuteFunction(
        request: ExecuteAppFunctionRequest,
        callingPackage: String,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<ExecuteAppFunctionResponse, AppFunctionException>
    ) {
        // The implementation is dispatched via the AppFunctionConfiguration.Provider
    }
}
