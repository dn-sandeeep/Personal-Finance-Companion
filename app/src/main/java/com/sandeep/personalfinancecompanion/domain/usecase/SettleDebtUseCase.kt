package com.sandeep.personalfinancecompanion.domain.usecase

import com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository
import javax.inject.Inject

class SettleDebtUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String, isSettled: Boolean = true): Result<Unit> {
        return try {
            repository.updateSettlementStatus(transactionId, isSettled)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
