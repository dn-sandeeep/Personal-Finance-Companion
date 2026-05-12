package com.sandeep.personalfinancecompanion.presentation.debt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntry
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntryType
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPersonSummary
import com.sandeep.personalfinancecompanion.domain.repository.UdhaarRepository
import com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class PassbookViewModel @Inject constructor(
    private val udhaarRepository: UdhaarRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: String = checkNotNull(savedStateHandle["personId"])

    private val _uiState = MutableStateFlow(PassbookUiState())
    val uiState: StateFlow<PassbookUiState> = _uiState.asStateFlow()

    init {
        loadPassbook()
    }

    private fun loadPassbook() {
        viewModelScope.launch {
            combine(
                udhaarRepository.getSummaries(),
                userPreferencesRepository.currencyFlow
            ) { summaries, currency ->
                val summary = summaries.find { it.person.id == personId }
                PassbookUiState(
                    summary = summary,
                    selectedCurrency = currency,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun saveEntry(amount: Double, isGiven: Boolean, note: String, date: Long) {
        viewModelScope.launch {
            val currentSummary = _uiState.value.summary
            val currentNet = currentSummary?.netAmount ?: 0.0
            val isOwedToYou = currentSummary?.isOwedToYou ?: true
            
            val finalType = when {
                isGiven -> {
                    // You gave money (+). If you owed them, it's PAID_BACK. Otherwise, it's GIVEN.
                    if (!isOwedToYou && currentNet > 0) UdhaarEntryType.PAID_BACK else UdhaarEntryType.GIVEN
                }
                else -> {
                    // You got money (-). If they owed you, it's RECEIVED_BACK. Otherwise, it's TAKEN.
                    if (isOwedToYou && currentNet > 0) UdhaarEntryType.RECEIVED_BACK else UdhaarEntryType.TAKEN
                }
            }

            udhaarRepository.addEntry(
                UdhaarEntry(
                    id = UUID.randomUUID().toString(),
                    personId = personId,
                    amount = amount,
                    type = finalType,
                    note = note.trim(),
                    date = date
                )
            )
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            udhaarRepository.deleteEntry(entryId)
        }
    }
}

data class PassbookUiState(
    val summary: UdhaarPersonSummary? = null,
    val selectedCurrency: Currency = Currency.INR,
    val isLoading: Boolean = true
)
