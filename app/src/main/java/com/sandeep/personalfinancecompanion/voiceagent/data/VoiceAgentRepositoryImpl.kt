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

    override suspend fun parse(context: Context, text: String): List<VoiceAgentResult> {
        // Try Gemini (High Accuracy) first
        val geminiResults = geminiSource.extractTransactionData(text)
        
        if (geminiResults.isNotEmpty()) {
            return geminiResults
        }

        // Mandatory Fallback to ML Kit (Offline/Error)
        val legacyResults = mlKitParser.parse(context, text)
        
        return legacyResults.map { legacy ->
            VoiceAgentResult(
                amount = legacy.amount,
                category = legacy.category,
                type = legacy.type,
                notes = legacy.notes,
                confidence = legacy.confidence,
                isReadyToSave = false // Require confirmation for offline fallback
            )
        }
    }
}
