package com.sandeep.personalfinancecompanion.presentation.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.usecase.GetUnsettledUdhaarUseCase
import com.sandeep.personalfinancecompanion.domain.usecase.SettleDebtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.combine

@HiltViewModel
class DebtManagementViewModel @Inject constructor(
    private val getUnsettledUdhaarUseCase: GetUnsettledUdhaarUseCase,
    private val settleDebtUseCase: SettleDebtUseCase,
    private val addTransactionUseCase: com.sandeep.personalfinancecompanion.domain.usecase.AddTransactionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()

    init {
        loadDebts()
    }

    private fun loadDebts() {
        viewModelScope.launch {
            combine(
                getUnsettledUdhaarUseCase(),
                userPreferencesRepository.currencyFlow
            ) { debts, currency ->
                // Calculate global totals
                val totalBorrowed = debts.filter { it.type == TransactionType.BORROWED }.sumOf { it.amount } - 
                                   debts.filter { it.type == TransactionType.BORROWED_REPAYMENT }.sumOf { it.amount }
                
                val totalLent = debts.filter { it.type == TransactionType.LENT }.sumOf { it.amount } - 
                               debts.filter { it.type == TransactionType.LENT_REPAYMENT }.sumOf { it.amount }

                // Group by peerName
                val summaries = debts
                    .filter { !it.peerName.isNullOrBlank() }
                    .groupBy { it.peerName!! }
                    .map { (name, peerTransactions) ->
                        val lentAmount = peerTransactions
                            .filter { it.type == TransactionType.LENT }
                            .sumOf { it.amount }
                        val lentRepaid = peerTransactions
                            .filter { it.type == TransactionType.LENT_REPAYMENT }
                            .sumOf { it.amount }
                        
                        val borrowedAmount = peerTransactions
                            .filter { it.type == TransactionType.BORROWED }
                            .sumOf { it.amount }
                        val borrowedRepaid = peerTransactions
                            .filter { it.type == TransactionType.BORROWED_REPAYMENT }
                            .sumOf { it.amount }

                        val netLent = lentAmount - lentRepaid
                        val netBorrowed = borrowedAmount - borrowedRepaid
                        
                        val balance = netLent - netBorrowed
                        
                        PeerDebtSummary(
                            peerName = name,
                            netAmount = kotlin.math.abs(balance),
                            isOwedToYou = balance > 0,
                            history = peerTransactions.sortedByDescending { it.date }
                        )
                    }
                    .filter { it.netAmount > 0 }
                    .sortedByDescending { it.netAmount }

                DebtUiState(
                    peerSummaries = summaries,
                    totalBorrowed = if (totalBorrowed > 0) totalBorrowed else 0.0,
                    totalLent = if (totalLent > 0) totalLent else 0.0,
                    selectedCurrency = currency,
                    isLoading = false
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun recordRepayment(peerName: String, amount: Double, isOwedToYou: Boolean, notes: String = "Repayment") {
        viewModelScope.launch {
            val repaymentType = if (isOwedToYou) TransactionType.LENT_REPAYMENT else TransactionType.BORROWED_REPAYMENT
            addTransactionUseCase(
                amount = amount,
                category = com.sandeep.personalfinancecompanion.domain.model.Category.UDHAAR,
                type = repaymentType,
                notes = notes,
                peerName = peerName
            )
        }
    }

    fun settleAllForPeer(peerName: String) {
        viewModelScope.launch {
            val peerSummary = _uiState.value.peerSummaries.find { it.peerName == peerName }
            peerSummary?.history?.forEach { transaction ->
                settleDebtUseCase(transaction.id)
            }
        }
    }
}

data class PeerDebtSummary(
    val peerName: String,
    val netAmount: Double,
    val isOwedToYou: Boolean,
    val history: List<Transaction>
)

data class DebtUiState(
    val peerSummaries: List<PeerDebtSummary> = emptyList(),
    val totalBorrowed: Double = 0.0,
    val totalLent: Double = 0.0,
    val selectedCurrency: Currency = Currency.INR,
    val isLoading: Boolean = true
)
