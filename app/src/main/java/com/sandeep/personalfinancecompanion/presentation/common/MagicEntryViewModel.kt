package com.sandeep.personalfinancecompanion.presentation.common

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.parser.ParsedTransaction
import com.sandeep.personalfinancecompanion.domain.repository.SmartParserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MagicEntryViewModel @Inject constructor(
    private val smartParserRepository: SmartParserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MagicEntryUiState())
    val uiState: StateFlow<MagicEntryUiState> = _uiState.asStateFlow()

    fun onTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(
            inputText = text,
            suggestions = smartParserRepository.getSuggestions(text)
        )
    }

    fun parseText(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = smartParserRepository.parseText(context, _uiState.value.inputText)
            _uiState.value = _uiState.value.copy(
                parsedTransaction = result,
                isLoading = false,
                showPreview = result.amount != null
            )
        }
    }

    fun onSuggestionClick(suggestion: String) {
        val currentText = _uiState.value.inputText
        val newText = if (currentText.isEmpty()) suggestion else "$currentText $suggestion"
        onTextChanged(newText)
    }

    fun clear() {
        _uiState.value = MagicEntryUiState()
    }
}

data class MagicEntryUiState(
    val inputText: String = "",
    val suggestions: List<String> = emptyList(),
    val parsedTransaction: ParsedTransaction? = null,
    val isLoading: Boolean = false,
    val showPreview: Boolean = false
)
