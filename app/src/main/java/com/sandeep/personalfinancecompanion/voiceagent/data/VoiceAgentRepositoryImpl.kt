package com.sandeep.personalfinancecompanion.voiceagent.data

import android.content.Context
import com.sandeep.personalfinancecompanion.domain.parser.SmartTransactionParser
import com.sandeep.personalfinancecompanion.voiceagent.domain.VoiceAgentParser
import com.sandeep.personalfinancecompanion.voiceagent.domain.VoiceAgentResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceAgentRepositoryImpl @Inject constructor(
    private val geminiSource: GeminiDataSource,
    private val mlKitParser: SmartTransactionParser
) : VoiceAgentParser {

    override suspend fun parse(context: Context, text: String): VoiceAgentResult {
        // Try Gemini (High Accuracy) first
        val geminiResult = geminiSource.extractTransactionData(text)
        
        if (geminiResult != null && geminiResult.amount != null) {
            return geminiResult
        }

        // Mandatory Fallback to ML Kit (Offline/Error)
        val legacyResult = mlKitParser.parse(context, text)
        
        return VoiceAgentResult(
            amount = legacyResult.amount,
            category = legacyResult.category,
            type = legacyResult.type,
            notes = legacyResult.notes,
            confidence = legacyResult.confidence,
            isReadyToSave = false // Require confirmation for fallback
        )
    }
}
