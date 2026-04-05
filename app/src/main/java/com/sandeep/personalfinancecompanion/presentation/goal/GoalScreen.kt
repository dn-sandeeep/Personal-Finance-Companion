package com.sandeep.personalfinancecompanion.presentation.goal

import android.graphics.Color.parseColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.model.Goal
import com.sandeep.personalfinancecompanion.domain.model.NoSpendStreak
import com.sandeep.personalfinancecompanion.domain.model.SavingVelocity
import com.sandeep.personalfinancecompanion.domain.model.VelocityStatus
import com.sandeep.personalfinancecompanion.presentation.components.EmptyState
import com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    viewModel: GoalViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is GoalUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        }

        is GoalUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "❌", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.retry() },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Retry")
                }
            }
        }

        is GoalUiState.Success -> {
            GoalContent(
                goals = state.goals,
                currency = state.currency,
                noSpendStreak = state.noSpendStreak,
                savingVelocity = state.savingVelocity,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalContent(
    goals: List<Goal>,
    currency: Currency,
    noSpendStreak: NoSpendStreak,
    savingVelocity: SavingVelocity,
    viewModel: GoalViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<Goal?>(null) }
    var showAddSavingsDialog by remember { mutableStateOf<Goal?>(null) }
    var showNoSpendCalendar by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<Goal?>(null) }
    var showEditGoalDialog by remember { mutableStateOf<Goal?>(null) }

    val sheetState = rememberModalBottomSheetState()

    if (showAddGoalDialog) {
        GoalTypePickerDialog(
            currency = currency,
            onDismiss = { showAddGoalDialog = false },
            onGoalSelected = { title, target, icon, color, date ->
                viewModel.createNewGoal(title, target, icon, color, date)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddSavingsDialog != null) {
        AddSavingsDialog(
            goalTitle = showAddSavingsDialog?.title ?: "",
            currency = currency,
            onDismiss = { showAddSavingsDialog = null },
            onConfirm = { amount ->
                showAddSavingsDialog?.let { viewModel.addSavings(it.id, amount) }
                showAddSavingsDialog = null
                selectedGoal = null // Close sheet too
            }
        )
    }

    if (showTargetDialog) {
        NoSpendTargetDialog(
            currentWeight = noSpendStreak.targetDays,
            onDismiss = { showTargetDialog = false },
            onConfirm = { days ->
                viewModel.setNoSpendTarget(days)
                showTargetDialog = false
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Goal?") },
            text = { Text("Are you sure you want to delete '${showDeleteConfirm?.title}'? This will also remove all savings history for this goal.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm?.let { viewModel.deleteGoal(it.id) }
                        showDeleteConfirm = null
                        selectedGoal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) { Text("Delete", color = colorScheme.onError) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }

    if (showEditGoalDialog != null) {
        EditGoalDialog(
            goal = showEditGoalDialog!!,
            currency = currency,
            onDismiss = { showEditGoalDialog = null },
            onConfirm = { title, amount, icon, color, date ->
                viewModel.updateGoalDetails(showEditGoalDialog!!.id, title, amount, icon, color, date)
                showEditGoalDialog = null
                selectedGoal = null
            }
        )
    }

    if (showNoSpendCalendar) {
        ModalBottomSheet(
            onDismissRequest = { showNoSpendCalendar = false },
            sheetState = sheetState,
            containerColor = colorScheme.surface
        ) {
            NoSpendCalendarBottomSheet(
                streak = noSpendStreak,
                onTargetClick = { showTargetDialog = true }
            )
        }
    }

    if (selectedGoal != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedGoal = null },
            sheetState = sheetState,
            containerColor = colorScheme.surface
        ) {
            GoalDetailBottomSheet(
                goal = selectedGoal!!,
                currency = currency,
                onAddMoneyClick = { showAddSavingsDialog = it },
                onEditClick = { showEditGoalDialog = it },
                onDeleteClick = { showDeleteConfirm = it },
                onPriorityChange = { goalId, priority ->
                    viewModel.updateGoalPriority(goalId, priority)
                    selectedGoal = null // Close sheet after priority change to refresh state
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        // Content Padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Active Goals",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tracking your path to financial equilibrium.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            val primaryGoal = goals.find { it.priority == 1 }
            PrimaryObjectiveCard(
                goal = primaryGoal,
                currency = currency,
                onSetPrimaryClick = { showAddGoalDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            NoSpendChallengeCard(
                streakData = noSpendStreak,
                currency = currency,
                onClick = { showNoSpendCalendar = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (goals.isEmpty()) {
                EmptyState(
                    emoji = "🎯",
                    title = "No active goals",
                    subtitle = "Set your first financial milestone today!"
                )
            } else {
                goals.forEach { goal ->
                    SmallGoalCard(
                        icon = getIconForName(goal.iconName),
                        iconBgColor = Color(parseColor(goal.colorHex)).copy(alpha = 0.2f),
                        iconTintColor = Color(parseColor(goal.colorHex)),
                        title = goal.title,
                        progress = goal.progress,
                        isOverdue = goal.isOverdue,
                        daysRemaining = goal.daysRemaining,
                        priority = goal.priority,
                        progressColor = Color(parseColor(goal.colorHex)),
                        trackColor = colorScheme.outlineVariant,
                        onClick = { selectedGoal = goal },
                        onEditClick = { showEditGoalDialog = goal }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            CreateNewGoalButton(onClick = { showAddGoalDialog = true })

            Spacer(modifier = Modifier.height(16.dp))

            SavingVelocityCard(velocity = savingVelocity)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTypePickerDialog(
    currency: Currency,
    onDismiss: () -> Unit,
    onGoalSelected: (String, Double, String, String, Long?) -> Unit
) {
    var isCustomMode by remember { mutableStateOf(false) }
    var selectedGoalIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Custom Goal States
    var customTitle by remember { mutableStateOf("") }
    var customAmount by remember { mutableStateOf("") }
    var customIcon by remember { mutableStateOf("Star") }
    var customColor by remember { mutableStateOf("#4CAF50") }

    val predefinedGoals = remember(currency) {
        listOf(
            Triple("New Car", Currency.convert(25000.0, Currency.USD, currency), "DirectionsCar"),
            Triple("Dream Home", Currency.convert(500000.0, Currency.USD, currency), "Home"),
            Triple("New Phone", Currency.convert(1000.0, Currency.USD, currency), "Smartphone"),
            Triple("Education", Currency.convert(15000.0, Currency.USD, currency), "School"),
            Triple("Retirement", Currency.convert(1000000.0, Currency.USD, currency), "TrendingUp")
        )
    }
    val colors = listOf("#F44336", "#2196F3", "#4CAF50", "#FF9800", "#9C27B0", "#E91E63", "#00BCD4", "#607D8B")
    val icons = listOf("DirectionsCar", "Home", "Smartphone", "School", "TrendingUp", "Star", "FlightTakeoff", "Security", "BeachAccess", "Celebration", "Devices", "Fastfood", "MedicalServices", "FitnessCenter")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCustomMode) "Create Custom Goal" else "Select Goal Type") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!isCustomMode) {
                    predefinedGoals.forEachIndexed { index, (title, target, icon) ->
                        val color = colors[index % colors.size]
                        val isSelected = selectedGoalIndex == index
                        
                        Surface(
                            onClick = { selectedGoalIndex = index },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getIconForName(icon),
                                    contentDescription = null,
                                    tint = Color(parseColor(color))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = CurrencyFormatter.formatAmount(target, currency),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { isCustomMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Your Own Goal")
                        }
                    }
                } else {
                    // Custom Mode Form
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text("Goal Name") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Wedding, Europe Trip") }
                        )

                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { input ->
                                val clean = input.replace(",", "").filter { it.isDigit() || it == '.' }
                                if (clean.isEmpty()) {
                                    customAmount = ""
                                } else {
                                    try {
                                        val parts = clean.split(".")
                                        val integerPart = parts[0].toLongOrNull() ?: 0L
                                        val formatted = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(integerPart)
                                        customAmount = when {
                                            parts.size > 1 -> "$formatted.${parts[1].take(2)}"
                                            clean.endsWith(".") -> "$formatted."
                                            else -> formatted
                                        }
                                    } catch (e: Exception) {
                                        customAmount = clean
                                    }
                                }
                            },
                            label = { Text("Target Amount") },
                            modifier = Modifier.fillMaxWidth(),
                            prefix = { Text(currency.symbol) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )

                        Text(text = "Choose Icon", style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            icons.forEach { icon ->
                                val isSelected = customIcon == icon
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { customIcon = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconForName(icon),
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(text = "Choose Color", style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colors.forEach { color ->
                                val isSelected = customColor == color
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(parseColor(color)))
                                        .border(
                                            2.dp,
                                            if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { customColor = color }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedDate == null) "Add Target Date (Optional)"
                            else "Target: ${
                                java.text.SimpleDateFormat(
                                    "MMM dd, yyyy",
                                    Locale.getDefault()
                                ).format(java.util.Date(selectedDate!!))
                            }"
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCustomMode) {
                        onGoalSelected(
                            customTitle,
                            customAmount.replace(",", "").toDoubleOrNull() ?: 0.0,
                            customIcon,
                            customColor,
                            selectedDate
                        )
                    } else {
                        selectedGoalIndex?.let { index ->
                            val (title, target, icon) = predefinedGoals[index]
                            val color = colors[index % colors.size]
                            onGoalSelected(title, target, icon, color, selectedDate)
                        }
                    }
                },
                enabled = if (isCustomMode) customTitle.isNotBlank() && customAmount.isNotBlank() else selectedGoalIndex != null
            ) {
                Text(if (isCustomMode) "Create Goal" else "Confirm Goal")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isCustomMode) isCustomMode = false
                    else onDismiss()
                }
            ) {
                Text(if (isCustomMode) "Back" else "Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

fun getIconForName(name: String): ImageVector {
    return when (name) {
        "FlightTakeoff" -> Icons.Default.FlightTakeoff
        "Security" -> Icons.Default.Security
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Home" -> Icons.Default.Home
        "Smartphone" -> Icons.Default.Smartphone
        "School" -> Icons.Default.School
        "TrendingUp" -> Icons.Default.TrendingUp
        "BeachAccess" -> Icons.Default.BeachAccess
        "Celebration" -> Icons.Default.Celebration
        "Devices" -> Icons.Default.Devices
        "Fastfood" -> Icons.Default.Fastfood
        "MedicalServices" -> Icons.Default.MedicalServices
        "FitnessCenter" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Star
    }
}

@Composable
private fun PrimaryObjectiveCard(
    goal: Goal?,
    currency: Currency,
    onSetPrimaryClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    
    if (goal == null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSetPrimaryClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Primary Objective Set",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ready for a new target? Set a primary objective to focus your progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSetPrimaryClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Set Primary Objective", fontSize = 12.sp)
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PRIMARY OBJECTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${(goal.progress * 100).toInt()}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.primary
                        )
                        Text(
                            text = "Complete",
                            fontSize = 11.sp,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = { goal.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Saved",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatAmount(goal.savedAmount, currency),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp)
                            .background(colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatAmount(goal.targetAmount, currency),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoSpendChallengeCard(
    streakData: NoSpendStreak,
    currency: Currency,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val cardBackground = if (streakData.hasSpentToday) {
        Brush.linearGradient(
            colors = listOf(colorScheme.surface, colorScheme.surfaceVariant)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(colorScheme.primaryContainer, colorScheme.surfaceVariant)
        )
    }

    val statusColor = if (streakData.hasSpentToday) {
        colorScheme.error
    } else {
        colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardBackground)
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    statusColor.copy(alpha = 0.15f),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (streakData.hasSpentToday) Icons.Default.Close else Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Spend Challenge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = streakData.message,
                            fontSize = 13.sp,
                            color = statusColor.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                    }

                    // Circular Progress
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = {
                                (streakData.currentStreak.toFloat() / streakData.targetDays.toFloat()).coerceIn(
                                    0f,
                                    1f
                                )
                            },
                            modifier = Modifier.size(70.dp),
                            color = statusColor,
                            strokeWidth = 6.dp,
                            trackColor = statusColor.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${streakData.currentStreak}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = statusColor
                            )
                            Text(
                                text = "/${streakData.targetDays}",
                                fontSize = 10.sp,
                                color = statusColor.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mini Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.05f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NoSpendMiniStat(
                        label = "BEST STREAK",
                        value = "${streakData.bestStreak}d",
                        color = statusColor
                    )
                    Divider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp),
                        color = statusColor.copy(alpha = 0.1f)
                    )

                    NoSpendMiniStat(
                        label = "POTENTIAL SAVINGS",
                        value = CurrencyFormatter.formatAmount(
                            streakData.potentialSavings,
                            currency
                        ),
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
private fun NoSpendMiniStat(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun NoSpendTargetDialog(
    currentWeight: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val options = listOf(7, 14, 21, 30, 60, 90)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Challenge Target") },
        text = {
            Column {
                options.forEach { days ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(days) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = days == currentWeight,
                            onClick = { onConfirm(days) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$days Days Challenge",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun NoSpendCalendarBottomSheet(
    streak: NoSpendStreak,
    onTargetClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
    ) {
        Text(
            text = "No Spend Calendar",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        Text(
            text = "Track your discipline over the last 30 days.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Grid of 30 days
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            val rows = 5
            val cols = 6
            val oneDayMillis = 24 * 60 * 60 * 1000L
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (c in 0 until cols) {
                        val dayIndex = r * cols + c
                        val dayTimestamp = today - (dayIndex * oneDayMillis)
                        val isNoSpend = streak.noSpendDays.contains(dayTimestamp)

                        Box(
                            modifier = Modifier
                                .padding(6.dp)
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isNoSpend) colorScheme.primary.copy(alpha = 0.1f)
                                    else colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (dayIndex == 0) colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isNoSpend) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                val dayOfMonth =
                                    Calendar.getInstance().apply { timeInMillis = dayTimestamp }
                                        .get(Calendar.DAY_OF_MONTH)
                                Text(
                                    text = "$dayOfMonth",
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onTargetClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer
            )
        ) {
            Icon(Icons.Default.TrendingUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adjust Challenge Target")
        }
    }
}

@Composable
private fun SmallGoalCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    title: String,
    progress: Float,
    isOverdue: Boolean,
    daysRemaining: Int?,
    priority: Int,
    progressColor: Color,
    trackColor: Color,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        
                        if (priority > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val priorityLabel = when(priority) {
                                1 -> "PRIMARY"
                                2 -> "SECONDARY"
                                3 -> "TERTIARY"
                                else -> ""
                            }
                            val priorityColor = when(priority) {
                                1 -> colorScheme.primary
                                2 -> colorScheme.secondary
                                3 -> colorScheme.tertiary
                                else -> colorScheme.outline
                            }
                            Box(
                                modifier = Modifier
                                    .background(priorityColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = priorityLabel,
                                    color = priorityColor,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (isOverdue) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    colorScheme.error.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OVERDUE",
                                color = colorScheme.error,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOverdue) "Past Deadline" else if (daysRemaining != null) "$daysRemaining days left" else "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) colorScheme.error else colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.fillMaxWidth(1f)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = trackColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateNewGoalButton(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .drawBehind {
                drawRoundRect(
                    color = colorScheme.outlineVariant,
                    style = Stroke(
                        width = 4f
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colorScheme.onSurface.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = colorScheme.surface,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create New Goal",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
    }
}
@Composable
private fun SavingVelocityCard(velocity: SavingVelocity) {
    if (velocity.status == VelocityStatus.EMPTY) return

    val colorScheme = MaterialTheme.colorScheme
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        SavingVelocityInfoDialog(onDismiss = { showInfoDialog = false })
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVING VELOCITY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Help",
                        tint = colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (velocity.status) {
                VelocityStatus.ANALYZING -> {
                    Text(
                        text = "Analyzing your saving cycle...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Keep saving! We'll have your velocity stats ready in about 3 days.",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
                else -> {
                    val statusText = when (velocity.status) {
                        VelocityStatus.AHEAD -> "Ahead of schedule"
                        VelocityStatus.BEHIND -> "Behind schedule"
                        else -> "On track"
                    }
                    val diffText = if (velocity.diffWeeks != 0) {
                        " ${Math.abs(velocity.diffWeeks)} weeks ${if (velocity.diffWeeks > 0) "early" else "late"}"
                    } else ""

                    Text(
                        text = "$statusText to reach your\n${velocity.primaryGoalTitle} goal$diffText.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    )

                    velocity.acceleratorCategory?.let { category ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your reduced spending in '${category.displayName}'\nover the last 14 days has\naccelerated your progress.",
                            fontSize = 13.sp,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                velocity.recentDailies.forEachIndexed { index, fraction ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(fraction.coerceIn(0.1f, 1f))
                            .padding(horizontal = 4.dp)
                            .background(
                                colorScheme.primary.copy(alpha = 0.2f + (0.2f * index)),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SavingVelocityInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("How Velocity Works") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Goal Selection", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("We analyze your highest priority goal, or the one with the closest deadline.", fontSize = 13.sp)
                }
                Column {
                    Text("Bonus Filter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("One-off large saves (>300% of average) are excluded from pace predictions to keep your estimates realistic.", fontSize = 13.sp)
                }
                Column {
                    Text("Accelerators", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("We look for categories where you're spending less than usual and showing you how that helps your savings.", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it") }
        }
    )
}

@Composable
fun AddSavingsDialog(
    goalTitle: String,
    currency: Currency,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings to $goalTitle") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${currency.symbol})") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amountText.toDoubleOrNull()?.let { onConfirm(it) }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailBottomSheet(
    goal: Goal,
    currency: Currency,
    onAddMoneyClick: (Goal) -> Unit,
    onEditClick: (Goal) -> Unit,
    onDeleteClick: (Goal) -> Unit,
    onPriorityChange: (String, Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeFormatter = remember {
        java.text.SimpleDateFormat(
            "MMM dd, yyyy • hh:mm a",
            java.util.Locale.getDefault()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onAddMoneyClick(goal) }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Money",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { onEditClick(goal) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Goal",
                        tint = colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { onDeleteClick(goal) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Goal",
                        tint = colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Progress Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Saved",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Target",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = CurrencyFormatter.formatAmount(goal.savedAmount, currency),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Text(
                        text = CurrencyFormatter.formatAmount(goal.targetAmount, currency),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { goal.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = colorScheme.primary,
                    trackColor = colorScheme.outlineVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Deadline Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant.copy(
                    alpha = 0.2f
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (goal.isOverdue) colorScheme.error else colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Target Deadline",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (goal.targetDate != null)
                                java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    .format(java.util.Date(goal.targetDate))
                            else "Not set",
                            fontWeight = FontWeight.Bold,
                            color = if (goal.isOverdue) colorScheme.error else colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Assign Priority",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val priorities = listOf(
                Triple(1, "Primary", colorScheme.primary),
                Triple(2, "Secondary", colorScheme.secondary),
                Triple(3, "Tertiary", colorScheme.tertiary),
                Triple(0, "None", colorScheme.outline)
            )

            priorities.forEach { (rank, label, color) ->
                val isSelected = goal.priority == rank
                Surface(
                    onClick = { onPriorityChange(goal.id, rank) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) color.copy(alpha = 0.15f) else colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (isSelected) color else colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) color else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Savings History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (goal.contributions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No savings recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                goal.contributions.reversed().forEach { contribution ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Deposit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = timeFormatter.format(java.util.Date(contribution.date)),
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "+${
                                CurrencyFormatter.formatAmount(
                                    contribution.amount,
                                    currency
                                )
                            }",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalDialog(
    goal: Goal,
    currency: Currency,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf(goal.title) }
    var targetAmount by remember {
        val initial = goal.targetAmount
        val formatted = try {
            java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(initial.toLong())
        } catch (e: Exception) {
            initial.toString()
        }
        mutableStateOf(formatted)
    }
    var iconName by remember { mutableStateOf(goal.iconName) }
    var colorHex by remember { mutableStateOf(goal.colorHex) }
    var selectedDate by remember { mutableStateOf(goal.targetDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = goal.targetDate)

    val icons = listOf("DirectionsCar", "Home", "Smartphone", "School", "TrendingUp", "Star", "FlightTakeoff", "Security", "BeachAccess", "Celebration", "Devices", "Fastfood", "MedicalServices", "FitnessCenter")
    val colors = listOf("#F44336", "#2196F3", "#4CAF50", "#FF9800", "#9C27B0", "#E91E63", "#00BCD4", "#607D8B")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Goal Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { input ->
                        val clean = input.replace(",", "").filter { it.isDigit() || it == '.' }
                        if (clean.isEmpty()) {
                            targetAmount = ""
                        } else {
                            try {
                                val parts = clean.split(".")
                                val integerPart = parts[0].toLongOrNull() ?: 0L
                                val formatted = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US).format(integerPart)
                                targetAmount = when {
                                    parts.size > 1 -> "$formatted.${parts[1].take(2)}"
                                    clean.endsWith(".") -> "$formatted."
                                    else -> formatted
                                }
                            } catch (e: Exception) {
                                targetAmount = clean
                            }
                        }
                    },
                    label = { Text("Target Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text(currency.symbol) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Text(text = "Choose Icon", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.chunked(4).forEach { chunk ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            chunk.forEach { icon ->
                                val isSelected = iconName == icon
                                IconButton(
                                    onClick = { iconName = icon },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            if (isSelected) 2.dp else 0.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = getIconForName(icon),
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Re-doing the layout for icons as the nested column logic above is a bit weird for a Row.
                // Let's use a FlowRow or similar, but for simplicity just a scrollable Row.
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.forEach { icon ->
                        val isSelected = iconName == icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { iconName = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForName(icon),
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(text = "Choose Color", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        val isSelected = colorHex == color
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(parseColor(color)))
                                .border(
                                    2.dp,
                                    if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { colorHex = color }
                        )
                    }
                }

                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedDate == null) "Set Target Date (Optional)"
                            else "Target: ${
                                java.text.SimpleDateFormat(
                                    "MMM dd, yyyy",
                                    Locale.getDefault()
                                ).format(java.util.Date(selectedDate!!))
                            }"
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = targetAmount.replace(",", "").toDoubleOrNull() ?: goal.targetAmount
                    onConfirm(title, amount, iconName, colorHex, selectedDate)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
