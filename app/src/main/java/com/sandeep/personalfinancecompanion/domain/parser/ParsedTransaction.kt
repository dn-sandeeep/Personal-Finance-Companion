package com.sandeep.personalfinancecompanion.domain.parser

import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import java.util.Date

/**
 * Represents the structured result of a natural language parse.
 */
data class ParsedTransaction(
    val amount: Double? = null,
    val category: Category = Category.OTHER,
    val type: TransactionType = TransactionType.EXPENSE,
    val date: Date = Date(),
    val notes: String = "",
    val confidence: Float = 0f
)
