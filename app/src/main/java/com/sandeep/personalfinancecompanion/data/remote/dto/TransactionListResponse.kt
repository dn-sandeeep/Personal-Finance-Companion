package com.sandeep.personalfinancecompanion.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionListResponse(
    val transactions: List<TransactionDto>
)
