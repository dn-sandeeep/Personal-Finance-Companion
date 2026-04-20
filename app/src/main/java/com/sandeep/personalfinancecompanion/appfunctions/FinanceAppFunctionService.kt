package com.sandeep.personalfinancecompanion.appfunctions

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.AppFunctionFunctionNotFoundException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.service.AppFunctionConfiguration
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
/**
 * Service that handles App Function requests from the Android System.
 */
@RequiresApi(Build.VERSION_CODES.BAKLAVA)
class FinanceAppFunctionService : AppFunctionService(), AppFunctionConfiguration.Provider {

    override fun onCreate() {
        super.onCreate()
        Log.d("AI_AGENT", "FinanceAppFunctionService.onCreate() - System is starting the service!")
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FinanceAppFunctionsEntryPoint {
        fun financeAppFunctions(): FinanceAppFunctions
    }

    private val financeAppFunctions: FinanceAppFunctions by lazy {
        Log.d("AI_AGENT", "Initializing FinanceAppFunctions via Hilt EntryPoint")
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext, 
            FinanceAppFunctionsEntryPoint::class.java
        )
        entryPoint.financeAppFunctions()
    }

    init {
        Log.d("AI_AGENT", "FinanceAppFunctionService class instantiated")
    }

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() {
            Log.d("AI_AGENT", "System requested AppFunctionConfiguration")
            return AppFunctionConfiguration.Builder()
                .addEnclosingClassFactory(FinanceAppFunctions::class.java) {
                    financeAppFunctions
                }
                .build()
        }


    override suspend fun executeFunction(
        request: ExecuteAppFunctionRequest
    ): ExecuteAppFunctionResponse {
        val functionId = request.functionIdentifier
        val parameters = request.functionParameters
        
        Log.d("AI_AGENT", "Dispatching App Function: $functionId")

        val appFunctionContext = object : AppFunctionContext {
            override val context: Context = applicationContext
        }

        return try {
            val result: String = when (functionId) {
                "com.sandeep.personalfinancecompanion.appfunctions.FinanceAppFunctions#addExpense" -> {
                    val amount = parameters.getDouble("amount")
                    val category = parameters.getString("category")
                    val notes = parameters.getString("notes")
                    financeAppFunctions.addExpense(appFunctionContext, amount, category, notes)
                }
                "com.sandeep.personalfinancecompanion.appfunctions.FinanceAppFunctions#addIncome" -> {
                    val amount = parameters.getDouble("amount")
                    val category = parameters.getString("category")
                    val notes = parameters.getString("notes")
                    financeAppFunctions.addIncome(appFunctionContext, amount, category, notes)
                }
                "com.sandeep.personalfinancecompanion.appfunctions.FinanceAppFunctions#getBalance" -> {
                    financeAppFunctions.getBalance(appFunctionContext)
                }
                else -> throw AppFunctionFunctionNotFoundException("Function not found: $functionId")
            }

            Log.d("AI_AGENT", "Function execution completed: $result")
            
            val responseWrapper = FinanceAppFunctionResponse(result)
            val resultData = AppFunctionData.serialize(responseWrapper, FinanceAppFunctionResponse::class.java)
            ExecuteAppFunctionResponse.Success(resultData)
        } catch (e: Exception) {
            Log.e("AI_AGENT", "Error in dispatching $functionId", e)
            ExecuteAppFunctionResponse.Error(
                AppFunctionFunctionNotFoundException(e.message ?: "Execution failed")
            )
        }
    }
}
