package com.sandeep.personalfinancecompanion.voiceagent.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.domain.usecase.AddTransactionUseCase
import com.sandeep.personalfinancecompanion.voiceagent.domain.VoiceAgentParser
import com.sandeep.personalfinancecompanion.voiceagent.domain.VoiceAgentResult
import com.sandeep.personalfinancecompanion.voiceagent.voice.VoiceRecognizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceAgentViewModel @Inject constructor(
    private val agentParser: VoiceAgentParser,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceAgentUiState())
    val uiState: StateFlow<VoiceAgentUiState> = _uiState.asStateFlow()

    private var voiceManager: VoiceRecognizerManager? = null

    private var confirmedTranscript: String = ""

    fun initVoiceManager(context: Context) {
        if (voiceManager == null) {
            voiceManager = VoiceRecognizerManager(
                context = context,
                onResults = { text, isFinal ->
                    onVoiceResults(text, isFinal)
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isListening = false,
                        errorMessage = error
                    )
                },
                onStateChange = { isListening ->
                    _uiState.value = _uiState.value.copy(isListening = isListening)
                }
            )
        }
    }

    private fun onVoiceResults(text: String, isFinal: Boolean) {
        if (isFinal) {
            confirmedTranscript = if (confirmedTranscript.isBlank()) {
                text
            } else {
                "$confirmedTranscript $text"
            }.trim()
            
            _uiState.value = _uiState.value.copy(
                inputText = confirmedTranscript,
                errorMessage = null
            )
        } else {
            // Partial update: show confirmed + current partial
            val displayResults = if (confirmedTranscript.isBlank()) {
                text
            } else {
                "$confirmedTranscript $text"
            }.trim()
            
            _uiState.value = _uiState.value.copy(
                inputText = displayResults,
                errorMessage = null
            )
        }
    }

    fun onTextChanged(text: String) {
        // Manual edits or final state update
        confirmedTranscript = text
        _uiState.value = _uiState.value.copy(
            inputText = text,
            errorMessage = null
        )
    }

    fun startListening() {
        voiceManager?.startListening()
    }

    fun stopListening() {
        voiceManager?.stopListening()
    }

    fun parseAndProcess(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val results = agentParser.parse(context, _uiState.value.inputText)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                parsedResults = results,
                showPreview = results.size > 1 || results.any { !it.isReadyToSave }
            )

            // Auto-Save Logic: If the agent is confident and there's only one complete result
            if (results.size == 1) {
                val result = results.first()
                if (result.isReadyToSave && result.amount != null) {
                    saveTransactions(results)
                }
            }
        }
    }

    fun saveTransactions(results: List<VoiceAgentResult>) {
        viewModelScope.launch {
            var allSuccess = true
            results.forEach { result ->
                if (result.amount != null) {
                    val success = addTransactionUseCase(
                        amount = result.amount,
                        category = result.category,
                        type = result.type,
                        notes = result.notes
                    ).isSuccess
                    if (!success) allSuccess = false
                }
            }
            
            if (allSuccess) {
                _uiState.value = _uiState.value.copy(isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save some transactions")
            }
        }
    }

    fun clear() {
        confirmedTranscript = ""
        _uiState.value = VoiceAgentUiState()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
    }
}

data class VoiceAgentUiState(
    val inputText: String = "",
    val isListening: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val parsedResults: List<VoiceAgentResult> = emptyList(),
    val showPreview: Boolean = false
)
