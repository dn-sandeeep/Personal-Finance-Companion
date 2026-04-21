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

    fun initVoiceManager(context: Context) {
        if (voiceManager == null) {
            voiceManager = VoiceRecognizerManager(
                context = context,
                onResults = { text ->
                    onTextChanged(text)
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

    fun onTextChanged(text: String) {
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
            val result = agentParser.parse(context, _uiState.value.inputText)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                parsedResult = result,
                showPreview = !result.isReadyToSave // Show preview if not confident to auto-save
            )

            // Auto-Save Logic: If the agent is confident and data is complete, save immediately
            if (result.isReadyToSave && result.amount != null) {
                saveTransaction(
                    amount = result.amount,
                    category = result.category,
                    type = result.type,
                    notes = result.notes
                )
            }
        }
    }

    fun saveTransaction(amount: Double, category: Category, type: TransactionType, notes: String) {
        viewModelScope.launch {
            val success = addTransactionUseCase(
                amount = amount,
                category = category,
                type = type,
                notes = notes
            ).isSuccess
            
            if (success) {
                _uiState.value = _uiState.value.copy(isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save transaction")
            }
        }
    }

    fun clear() {
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
    val parsedResult: VoiceAgentResult? = null,
    val showPreview: Boolean = false
)
