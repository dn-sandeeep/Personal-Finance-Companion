package com.sandeep.personalfinancecompanion.presentation.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntry
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntryType
import com.sandeep.personalfinancecompanion.domain.model.UdhaarOverview
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPerson
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
class DebtManagementViewModel @Inject constructor(
    private val udhaarRepository: UdhaarRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()

    init {
        loadLedger()
    }

    private fun loadLedger() {
        viewModelScope.launch {
            combine(
                udhaarRepository.getSummaries(),
                udhaarRepository.getOverview(),
                userPreferencesRepository.currencyFlow
            ) { summaries, overview, currency ->
                DebtUiState(
                    peerSummaries = summaries,
                    overview = overview,
                    selectedCurrency = currency,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun saveEntry(
        personId: String?,
        personName: String,
        phoneNumber: String?,
        amount: Double,
        type: UdhaarEntryType,
        note: String,
        date: Long
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cleanedPhone = phoneNumber?.trim()?.takeIf { it.isNotBlank() }
            val existingPerson = _uiState.value.peerSummaries
                .map { it.person }
                .firstOrNull { it.id == personId }

            val person = existingPerson?.copy(
                name = personName.trim(),
                phoneNumber = cleanedPhone,
                updatedAt = now
            ) ?: UdhaarPerson(
                id = UUID.randomUUID().toString(),
                name = personName.trim(),
                phoneNumber = cleanedPhone,
                createdAt = now,
                updatedAt = now
            )

            udhaarRepository.savePerson(person)
            udhaarRepository.addEntry(
                UdhaarEntry(
                    id = UUID.randomUUID().toString(),
                    personId = person.id,
                    amount = amount,
                    type = type,
                    note = note.trim(),
                    date = date
                )
            )
        }
    }

    fun updatePerson(person: UdhaarPerson, name: String, phoneNumber: String?) {
        viewModelScope.launch {
            udhaarRepository.savePerson(
                person.copy(
                    name = name.trim(),
                    phoneNumber = phoneNumber?.trim()?.takeIf { it.isNotBlank() },
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}

data class DebtUiState(
    val peerSummaries: List<UdhaarPersonSummary> = emptyList(),
    val overview: UdhaarOverview = UdhaarOverview(),
    val selectedCurrency: Currency = Currency.INR,
    val isLoading: Boolean = true
)
