package com.sandeep.personalfinancecompanion.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val date: Long, // Unix timestamp in millis
    val notes: String
)
