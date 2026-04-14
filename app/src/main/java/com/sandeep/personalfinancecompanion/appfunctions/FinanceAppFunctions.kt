package com.sandeep.personalfinancecompanion.appfunctions

import androidx.appfunctions.service.AppFunction
import androidx.appfunctions.AppFunctionContext
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.usecase.AddTransactionUseCase
import com.sandeep.personalfinancecompanion.domain.usecase.CalculateBalanceUseCase
import com.sandeep.personalfinancecompanion.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Exposes core finance functionalities as App Functions for AI agents like Gemini.
 */
class FinanceAppFunctions @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val calculateBalanceUseCase: CalculateBalanceUseCase
) {

    /**
     * Adds an expense transaction to the app.
     * 
     * @param amount The numerical amount of the expense.
     * @param category The category of the expense (e.g., FOOD, TRANSPORT).
     * @param notes Optional notes or description for the expense.
     * @return A status message indicating success or failure.
     */
    @AppFunction
    suspend fun addExpense(
        context: AppFunctionContext,
        amount: Double,
        category: String,
        notes: String
    ): String {
        val categoryEnum = try {
            Category.valueOf(category.uppercase())
        } catch (e: Exception) {
            Category.OTHER
        }

        val result = addTransactionUseCase(
            amount = amount,
            category = categoryEnum,
            type = TransactionType.EXPENSE,
            notes = notes
        )
        return if (result.isSuccess) {
            "Successfully added expense of $amount in ${categoryEnum.displayName}"
        } else {
            "Failed to add expense: ${result.exceptionOrNull()?.message}"
        }
    }

    /**
     * Adds an income transaction to the app.
     * 
     * @param amount The numerical amount of the income.
     * @param category The category of the income (e.g., SALARY, FREELANCE).
     * @param notes Optional notes or description for the income.
     * @return A status message indicating success or failure.
     */
    @AppFunction
    suspend fun addIncome(
        context: AppFunctionContext,
        amount: Double,
        category: String,
        notes: String
    ): String {
        val categoryEnum = try {
            Category.valueOf(category.uppercase())
        } catch (e: Exception) {
            Category.OTHER
        }

        val result = addTransactionUseCase(
            amount = amount,
            category = categoryEnum,
            type = TransactionType.INCOME,
            notes = notes
        )
        return if (result.isSuccess) {
            "Successfully added income of $amount in ${categoryEnum.displayName}"
        } else {
            "Failed to add income: ${result.exceptionOrNull()?.message}"
        }
    }

    /**
     * Retrieves the current balance summary.
     * 
     * @return A string representing the total income, total expense, and current balance.
     */
    @AppFunction
    suspend fun getBalance(context: AppFunctionContext): String {
        val transactions = getTransactionsUseCase().first()
        val summary = calculateBalanceUseCase(transactions)
        return "Current Balance: ${summary.currentBalance}. (Total Income: ${summary.totalIncome}, Total Expense: ${summary.totalExpense})"
    }
}
