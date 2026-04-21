package com.sandeep.personalfinancecompanion.voiceagent.domain

import android.content.Context

interface VoiceAgentParser {
    suspend fun parse(context: Context, text: String): List<VoiceAgentResult>
}
