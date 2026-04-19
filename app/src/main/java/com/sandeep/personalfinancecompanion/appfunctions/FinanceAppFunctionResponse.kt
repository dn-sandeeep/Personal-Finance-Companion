package com.sandeep.personalfinancecompanion.appfunctions

import androidx.appfunctions.AppFunctionSerializable

/**
 * A generic response for App Functions that return a simple message.
 */
@AppFunctionSerializable
data class FinanceAppFunctionResponse(
    /**
     * The message returned by the function.
     */
    val message: String
)
