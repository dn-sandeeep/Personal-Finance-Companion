package com.sandeep.personalfinancecompanion.presentation.home

import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.presentation.components.BarEntry
import com.sandeep.personalfinancecompanion.presentation.components.BudgetRing
import com.sandeep.personalfinancecompanion.presentation.components.EmptyState
import com.sandeep.personalfinancecompanion.presentation.components.WeeklyTrendChart

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.presentation.components.TransactionListItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is HomeUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "⚠️", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.retry() }) {
                    Text("Retry")
                }
            }
        }

        is HomeUiState.Success -> {
            HomeContent(
                state = state,
                onNavigateToTransactions = onNavigateToTransactions,
                onAddIncome = onAddIncome,
                onAddExpense = onAddExpense,
                onDaySelected = { dayOfWeek ->
                    viewModel.selectDay(dayOfWeek)
                },
                onCategorySelected = { category ->
                    viewModel.selectCategory(category)
                },
                onUpdateBudget = { newLimit ->
                    viewModel.updateBudgetLimit(newLimit)
                }
            )

            if (state.selectedDayTransactions != null || state.selectedCategoryTransactions != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.clearSelectedDay() },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(width = 32.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                    }
                ) {
                    val label = state.selectedDayLabel ?: state.selectedCategoryLabel ?: ""
                    val txs = state.selectedDayTransactions ?: state.selectedCategoryTransactions
                    ?: emptyList()
                    DayDetailsContent(
                        dayLabel = label,
                        transactions = txs,
                        currency = state.selectedCurrency
                    )
                }
            }
        }
    }
}

@Composable
private fun DayDetailsContent(
    dayLabel: String,
    transactions: List<Transaction>,
    currency: Currency
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a, MMM dd, yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Transactions for $dayLabel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            EmptyState(
                emoji = "📅",
                title = "No transactions",
                subtitle = "You didn't record any transactions on this day."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                transactions.forEach { transaction ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Emoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = transaction.category.emoji)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = transaction.category.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = timeFormatter.format(Date(transaction.date)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (transaction.notes.isNotEmpty()) {
                                    Text(
                                        text = transaction.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Text(
                                text = if (transaction.type == TransactionType.INCOME) {
                                    "+${CurrencyFormatter.formatAmount(transaction.amount, currency)}"
                                } else {
                                    "-${CurrencyFormatter.formatAmount(transaction.amount, currency)}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (transaction.type == TransactionType.INCOME) Color(
                                    0xFF4ADE80
                                ) else Color(0xFFFB7185)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onNavigateToTransactions: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onCategorySelected: (com.sandeep.personalfinancecompanion.domain.model.Category) -> Unit,
    onUpdateBudget: (Double) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showBudgetDialog by remember { mutableStateOf(false) }

    if (showBudgetDialog) {
        EditBudgetDialog(
            currentLimit = state.budgetLimit,
            currency = state.selectedCurrency,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { newLimit ->
                showBudgetDialog = false
                onUpdateBudget(newLimit)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp)
    ) {

        // ──── Balance Card ────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primary,
                            colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "TOTAL BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onPrimary.copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Crossfade(targetState = state.selectedCurrency, label = "balance_anim") { currency ->
                    Text(
                        text = CurrencyFormatter.formatAmount(state.balance.currentBalance, currency),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 36.sp
                        ),
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Income & Expense pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Monthly Income
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.onPrimary.copy(alpha = 0.15f))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80), // Keep positive green for visibility
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "MONTHLY\nINCOME",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onPrimary.copy(alpha = 0.7f),
                                    lineHeight = 14.sp,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Crossfade(targetState = state.selectedCurrency, label = "income_anim") { currency ->
                                    Text(
                                        text = CurrencyFormatter.formatAmount(state.balance.totalIncome, currency),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Monthly Expenses
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.onPrimary.copy(alpha = 0.15f))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFFFB7185), // Keep negative red for visibility
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "MONTHLY\nEXPENSES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onPrimary.copy(alpha = 0.7f),
                                    lineHeight = 14.sp,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Crossfade(targetState = state.selectedCurrency, label = "expense_anim") { currency ->
                                    Text(
                                        text = CurrencyFormatter.formatAmount(state.balance.totalExpense, currency),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ──── Weekly Trend Card ────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "+12.5% ↗",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                WeeklyTrendChart(
                    entries = state.weeklyTrend,
                    onBarClick = { entry ->
                        onDaySelected(entry.dayOfWeek)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ──── Savings Goal Card ────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Savings Goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Monthly Budget Tracker",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                BudgetRing(
                    spent = state.totalExpense,
                    limit = state.budgetLimit,
                    size = 140.dp,
                    strokeWidth = 12.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { showBudgetDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Edit Budget",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ──── Category Breakdown ────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            TextButton(onClick = onNavigateToTransactions) {
                Text(
                    text = "VIEW ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.categoryExpenses.isEmpty()) {
            EmptyState(
                emoji = "💸",
                title = "No transactions yet",
                subtitle = "Tap + to add your first one!"
            )
        } else {
            // Premium Segmented Bar Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    state.categoryExpenses.forEach { stats ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(stats.percentage.coerceAtLeast(0.01f))
                                .background(getCategoryColor(stats.category))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            state.categoryExpenses.forEach { stats ->
                CategoryBreakdownItem(
                    stats = stats,
                    currency = state.selectedCurrency,
                    onClick = { onCategorySelected(stats.category) }
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
        }
    }

    Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
}


@Composable
private fun CategoryBreakdownItem(
    stats: CategoryStats,
    currency: Currency,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val categoryColor = getCategoryColor(stats.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Badge with custom background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stats.category.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stats.category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyFormatter.formatAmount(stats.amount, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stats.transactionCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(stats.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stats.percentage)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        categoryColor.copy(alpha = 0.7f),
                                        categoryColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

private fun getCategoryColor(category: Category): Color {
    return when (category) {
        Category.FOOD -> Color(0xFFFF9F1C)
        Category.TRANSPORT -> Color(0xFF2EC4B6)
        Category.SHOPPING -> Color(0xFFE71D36)
        Category.ENTERTAINMENT -> Color(0xFF9B5DE5)
        Category.BILLS -> Color(0xFF00B4D8)
        Category.HEALTH -> Color(0xFFF15BB5)
        Category.EDUCATION -> Color(0xFFFEE440)
        Category.OTHER -> Color(0xFFADB5BD)
        else -> Color(0xFF6C757D)
    }
}

@Composable
fun EditBudgetDialog(
    currentLimit: Double,
    currency: Currency,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetInput by remember { mutableStateOf(currentLimit.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Monthly Budget")
        },
        text = {
            Column {
                Text(
                    text = "Set a new monthly spending limit to track against.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { newValue: String -> budgetInput = newValue },
                    label = { Text("Amount (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newLimit = budgetInput.toDoubleOrNull()
                    if (newLimit != null && newLimit > 0) {
                        onConfirm(newLimit)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

