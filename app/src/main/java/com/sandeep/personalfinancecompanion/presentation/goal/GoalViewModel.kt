package com.sandeep.personalfinancecompanion.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.personalfinancecompanion.domain.model.Goal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor() : ViewModel() {
    
    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()
    
    init {
        // Initial goals
        _goals.value = listOf(
            Goal(
                id = UUID.randomUUID().toString(),
                title = "Travel Fund",
                targetAmount = 5000.0,
                savedAmount = 2100.0,
                iconName = "FlightTakeoff",
                colorHex = "#2196F3"
            ),
            Goal(
                id = UUID.randomUUID().toString(),
                title = "Emergency",
                targetAmount = 10000.0,
                savedAmount = 8800.0,
                iconName = "Security",
                colorHex = "#4CAF50"
            )
        )
    }
    
    fun createNewGoal(title: String, targetAmount: Double, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val newGoal = Goal(
                id = UUID.randomUUID().toString(),
                title = title,
                targetAmount = targetAmount,
                savedAmount = 0.0,
                iconName = iconName,
                colorHex = colorHex
            )
            _goals.value = _goals.value + newGoal
        }
    }
}
