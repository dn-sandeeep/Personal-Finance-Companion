package com.sandeep.personalfinancecompanion.voiceagent.domain

import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.TransactionType

/**
 * Represents the structured result of a voice agent's analysis.
 * Ported from the standalone Agent project.
 */
data class VoiceAgentResult(
    val amount: Double? = null,
    val category: Category = Category.OTHER,
    val type: TransactionType = TransactionType.EXPENSE,
    val notes: String = "",
    val peerName: String? = null,
    val confidence: Float = 1.0f,
    val isReadyToSave: Boolean = false
)
