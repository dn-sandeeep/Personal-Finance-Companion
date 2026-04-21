package com.sandeep.personalfinancecompanion.voiceagent.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

/**
 * Ported from the standalone Agent project.
 * Manages Speech-to-Text interaction.
 */
class VoiceRecognizerManager(
    private val context: Context,
    private val onResults: (String, Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onStateChange: (Boolean) -> Unit = {}
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isManualStopRequested = false

    fun startListening() {
        isManualStopRequested = false
        performStart()
    }

    private fun performStart() {
        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceRecognizerManager)
                }
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            
            speechRecognizer?.startListening(intent)
            onStateChange(true)
            Log.d("VoiceAgent", "Sticky listening session started...")
        } catch (e: Exception) {
            onError("Initialization error: ${e.message}")
        }
    }

    fun stopListening() {
        isManualStopRequested = true
        speechRecognizer?.stopListening()
        onStateChange(false)
        Log.d("VoiceAgent", "Manual stop requested.")
    }

    fun destroy() {
        isManualStopRequested = true
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        // Speech ended, but if not manually stopped, we stay in "Listening" UI state
        // and wait for results or restart
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
        
        Log.e("VoiceAgent", "Speech Error: $errorMessage ($error)")

        // Auto-restart if it was just a timeout and manual stop wasn't clicked
        if (!isManualStopRequested && (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH)) {
            Log.d("VoiceAgent", "Restarting after timeout/no match...")
            performStart()
        } else if (!isManualStopRequested) {
            onError(errorMessage)
            onStateChange(false)
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onResults(matches[0], true)
        }

        // AUTO-RESTART for sticky listening
        if (!isManualStopRequested) {
            Log.d("VoiceAgent", "Sticky restart after results...")
            performStart()
        } else {
            onStateChange(false)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onResults(matches[0], false)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
