package com.sandeep.personalfinancecompanion.domain.repository

import android.content.Context
import com.sandeep.personalfinancecompanion.domain.parser.ParsedTransaction
import kotlinx.coroutines.flow.Flow

interface SmartParserRepository {
    suspend fun parseText(context: Context, text: String): ParsedTransaction
    fun getSuggestions(text: String): List<String>
}
