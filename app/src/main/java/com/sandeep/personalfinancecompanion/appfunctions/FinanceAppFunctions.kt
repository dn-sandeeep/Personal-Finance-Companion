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
     * Records a new expense or spending transaction in the Track Spend finance tracker.
     * Use this function when the user mentions they spent money, paid for something, 
     * or made a purchase (e.g., "I spent 500 on pizza").
     * 
     * @param amount The numerical value of the expense (e.g., 50.0, 100.0).
     * @param category The category for this expense (e.g., FOOD, TRANSPORT, BILLS, SHOPPING).
     * @param notes A short description of the purchase or item bought.
     * @return A message confirming that the expense has been recorded successfully.
     */
    @AppFunction
    suspend fun addExpense(
        context: AppFunctionContext,
        amount: Double,
        category: String?,
        notes: String?
    ): String {
        val categoryEnum = try {
            Category.valueOf(category?.uppercase() ?: "OTHER")
        } catch (e: Exception) {
            Category.OTHER
        }

        val result = addTransactionUseCase(
            amount = amount,
            category = categoryEnum,
            type = TransactionType.EXPENSE,
            notes = notes ?: "Voice entry"
        )
        return if (result.isSuccess) {
            "Successfully added expense of $amount in ${categoryEnum.displayName}"
        } else {
            "Failed to add expense: ${result.exceptionOrNull()?.message}"
        }
    }

    /**
     * Records a new income or earnings transaction in the Track Spend tracker.
     * Use this function when the user mentions they received money, earned a salary, 
     * got a bonus, or received a gift (e.g., "I got my salary of 50000").
     * 
     * @param amount The numerical value of the income received.
     * @param category The source of this income (e.g., SALARY, FREELANCE, GIFT, INVESTMENT).
     * @param notes A short description or note about the source of income.
     * @return A message confirming that the income has been recorded successfully.
     */
    @AppFunction
    suspend fun addIncome(
        context: AppFunctionContext,
        amount: Double,
        category: String?,
        notes: String?
    ): String {
        val categoryEnum = try {
            Category.valueOf(category?.uppercase() ?: "OTHER")
        } catch (e: Exception) {
            Category.OTHER
        }

        val result = addTransactionUseCase(
            amount = amount,
            category = categoryEnum,
            type = TransactionType.INCOME,
            notes = notes ?: "Voice entry"
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
