package com.sandeep.personalfinancecompanion.domain.usecase

import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.repository.GoalRepository
import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ChangeCurrencyUseCase @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(newCurrency: Currency) {
        val oldCurrency = preferencesRepository.currencyFlow.first()
        if (oldCurrency == newCurrency) return

        val factor = Currency.convert(1.0, oldCurrency, newCurrency)

        // 1. Update global currency
        preferencesRepository.updateCurrency(newCurrency)

        // 2. Update budget limit
        val currentBudget = preferencesRepository.budgetLimitFlow.first()
        preferencesRepository.updateBudgetLimit(currentBudget * factor)

        // 3. Update all transactions
        transactionRepository.convertAllTransactions(factor)

        // 4. Update all goals and contributions
        goalRepository.convertAllGoalsAndContributions(factor)
    }
}
