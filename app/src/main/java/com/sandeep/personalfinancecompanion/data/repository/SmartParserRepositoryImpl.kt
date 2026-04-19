package com.sandeep.personalfinancecompanion.data.repository

import android.content.Context
import com.sandeep.personalfinancecompanion.domain.parser.ParsedTransaction
import com.sandeep.personalfinancecompanion.domain.parser.SmartTransactionParser
import com.sandeep.personalfinancecompanion.domain.repository.SmartParserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartParserRepositoryImpl @Inject constructor(
    private val parser: SmartTransactionParser
) : SmartParserRepository {

    override suspend fun parseText(context: Context, text: String): ParsedTransaction {
        return parser.parse(context, text)
    }

    override fun getSuggestions(text: String): List<String> {
        val lowerText = text.lowercase()
        return when {
            lowerText.isEmpty() -> listOf("Chai ke liye 50", "2000 rent paid", "Received 50000 salary")
            lowerText.contains("paid") || lowerText.contains("spent") -> listOf("for food", "for rent", "for transport")
            else -> listOf("for dinner", "for petrol", "for movie")
        }
    }
}
