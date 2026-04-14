package com.sandeep.personalfinancecompanion.appfunctions

import androidx.appfunctions.AppFunctionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service that handles App Function requests from the Android System.
 * Hilt is used to inject the actual implementation class.
 */
@AndroidEntryPoint
class FinanceAppFunctionService : AppFunctionService() {

    @Inject
    lateinit var financeAppFunctions: FinanceAppFunctions

    override fun getAppFunctionsInvoker(): Any {
        return financeAppFunctions
    }
}
